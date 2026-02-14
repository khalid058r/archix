package archix_base.document.service;

import archix_base.audit.service.AuditService;
import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.EntityNotFoundException;
import archix_base.common.storage.FileMetadata;
import archix_base.common.storage.StorageService;
import archix_base.common.storage.StorageUtils;
import archix_base.document.entity.Document;
import archix_base.document.entity.DocumentStatus;
import archix_base.document.entity.DocumentVersion;
import archix_base.document.repo.DocumentRepo;
import archix_base.document.repo.DocumentVersionRepo;
import archix_base.identity.entity.User;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;
import archix_base.organization.repo.NamespaceRepo;
import archix_base.organization.repo.OrganizationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * Document service with MinIO storage integration.
 * Files are stored in MinIO, only metadata in database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {

    private final DocumentRepo documentRepository;
    private final DocumentVersionRepo documentVersionRepository;
    private final NamespaceRepo namespaceRepository;
    private final UserRepo userRepository;
    private final OrganizationRepo organizationRepository;
    private final PermissionRepo permissionRepository;
    private final StorageService storageService;
    private final AuditService auditService;

    // ==================== READ OPERATIONS ====================

    public Document getById(Long id, Long organizationId, Long userId) {
        validateOrganizationAccess(organizationId, userId);

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));

        if (doc.getOrganization() == null || !doc.getOrganization().getId().equals(organizationId)) {
            throw new EntityNotFoundException("Document not found in this organization: " + id);
        }

        return doc;
    }

    /**
     * Get document by ID without organization check (for deriving org context).
     */
    @Transactional(readOnly = true)
    public Document getByIdWithoutOrgCheck(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));
    }

    /**
     * Get document content as a streaming resource.
     * Uses MinIO streaming - content is NOT loaded into memory.
     */
    @Transactional
    public Resource getDocumentContentAsResource(Long id, Long organizationId, Long userId) {
        Document doc = getById(id, organizationId, userId);

        if (doc.getStoragePath() == null) {
            throw new EntityNotFoundException("Document has no content stored: " + id);
        }

        try {
            InputStream inputStream = storageService.downloadFile(doc.getStoragePath());

            // Record access
            doc.recordAccess();
            documentRepository.save(doc);

            auditService.log("DOWNLOAD", "Document", id.toString(), userId, null,
                    "Downloaded document: " + doc.getFileName());

            return new InputStreamResource(inputStream);
        } catch (archix_base.common.storage.StorageException e) {
            log.warn("Storage file not found for document {}: {}", id, e.getMessage());
            throw new EntityNotFoundException("Document content not found in storage: " + id);
        } catch (Exception e) {
            log.error("Failed to download document {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to retrieve document content");
        }
    }

    /**
     * Get document content as bytes (for smaller files).
     */
    @Transactional
    public byte[] getDocumentContentAsBytes(Long id, Long organizationId, Long userId) {
        Document doc = getById(id, organizationId, userId);

        if (doc.getStoragePath() == null) {
            return new byte[0];
        }

        byte[] content = storageService.downloadFileAsBytes(doc.getStoragePath());

        doc.recordAccess();
        documentRepository.save(doc);

        return content;
    }

    /**
     * Generate a presigned URL for direct download (expires in N minutes).
     */
    public String generateDownloadUrl(Long id, Long organizationId, Long userId, int expirationMinutes) {
        Document doc = getById(id, organizationId, userId);

        if (doc.getStoragePath() == null) {
            throw new BadRequestException("Document has no content stored");
        }

        return storageService.generatePresignedUrl(doc.getStoragePath(), expirationMinutes);
    }

    /**
     * Get all versions of a document.
     */
    @Transactional(readOnly = true)
    public java.util.List<DocumentVersion> getVersions(Long documentId, Long organizationId, Long userId) {
        // First verify document exists and user has access
        getById(documentId, organizationId, userId);

        // Return all versions for this document
        return documentVersionRepository.findByDocumentId(documentId);
    }

    // ==================== CREATE OPERATIONS ====================

    /**
     * Create a document with file upload to MinIO.
     */
    @Transactional
    public Document create(MultipartFile file, String name, Long parentId,
            Long createdById, Long organizationId) {

        validateOrganizationAccess(organizationId, createdById);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + createdById));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + organizationId));

        // Check storage quota
        if (!organization.hasStorageSpace(file.getSize())) {
            throw new BadRequestException("Storage quota exceeded. Please upgrade your plan.");
        }

        // Validate file type
        String mimeType = file.getContentType();
        StorageUtils.validateFileType(mimeType);
        StorageUtils.validateFileSize(file.getSize());

        // Get parent namespace if provided
        Namespace parent = null;
        if (parentId != null) {
            parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + parentId));

            // Check if MIME type is allowed in this folder
            if (!parent.isMimeTypeAllowed(mimeType)) {
                throw new BadRequestException("File type not allowed in this folder");
            }
        }

        // Check for duplicate filename
        String originalFileName = file.getOriginalFilename();
        String documentName = name != null ? name : originalFileName;

        if (documentRepository.existsByFileNameAndParentIdAndOrganizationId(originalFileName, parentId,
                organizationId)) {
            throw new BadRequestException("A document with this filename already exists in this folder");
        }

        // Generate storage path and upload to MinIO
        String storagePath = StorageUtils.generateStoragePath(organizationId, originalFileName);

        try {
            storageService.uploadFile(file, storagePath);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage());
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }

        // Calculate checksum
        String checksum = null;
        try {
            checksum = StorageUtils.calculateChecksum(file.getInputStream());
        } catch (IOException e) {
            log.warn("Failed to calculate checksum: {}", e.getMessage());
        }

        // Create document entity
        Document doc = new Document();
        doc.setName(documentName);
        doc.setFileName(originalFileName);
        doc.setOriginalFileName(originalFileName);
        doc.setFileSize(file.getSize());
        doc.setMimeType(mimeType);
        doc.setStoragePath(storagePath);
        doc.setChecksum(checksum);
        doc.setCreatedBy(createdBy);
        doc.setOrganization(organization);
        doc.setParent(parent);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        doc.setStatus(DocumentStatus.DRAFT);
        doc.setVersion(1);
        doc.setIsLatestVersion(true);
        doc.setDownloadCount(0);
        doc.setProcessingStatus(Document.ProcessingStatus.PENDING);

        Document saved = documentRepository.save(doc);

        // Update organization storage usage
        organization.addStorageUsage(file.getSize());
        organizationRepository.save(organization);

        // Update parent namespace stats
        if (parent != null) {
            parent.addDocument(file.getSize());
            namespaceRepository.save(parent);
        }

        auditService.log("CREATE", "Document", saved.getId().toString(), createdById, createdBy.getEmail(),
                "Created document: " + saved.getFileName() + " (" + StorageUtils.formatBytes(file.getSize()) + ")");

        return saved;
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update document metadata (not file content).
     */
    @Transactional
    public Document updateMetadata(Long id, String name, String description, String tags,
            Long parentId, User actor, Long organizationId) {

        Document doc = getById(id, organizationId, actor.getId());

        if (name != null)
            doc.setName(name);
        if (description != null)
            doc.setDescription(description);
        if (tags != null)
            doc.setTags(tags);

        // Handle parent change
        if (parentId != null && (doc.getParent() == null || !parentId.equals(doc.getParent().getId()))) {
            Namespace newParent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + parentId));

            // Update old parent stats
            if (doc.getParent() != null) {
                doc.getParent().removeDocument(doc.getFileSize());
                namespaceRepository.save(doc.getParent());
            }

            // Update new parent stats
            newParent.addDocument(doc.getFileSize());
            namespaceRepository.save(newParent);

            doc.setParent(newParent);
        }

        doc.setUpdatedAt(LocalDateTime.now());
        Document saved = documentRepository.save(doc);

        auditService.log("UPDATE", "Document", saved.getId().toString(), actor.getId(), actor.getEmail(),
                "Updated document metadata");

        return saved;
    }

    /**
     * Upload new version of document.
     */
    @Transactional
    public Document uploadNewVersion(Long id, MultipartFile file, User actor, Long organizationId) {
        Document currentDoc = getById(id, organizationId, actor.getId());
        Organization organization = currentDoc.getOrganization();

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        // Check storage quota for the difference
        long sizeDiff = file.getSize() - currentDoc.getFileSize();
        if (sizeDiff > 0 && !organization.hasStorageSpace(sizeDiff)) {
            throw new BadRequestException("Storage quota exceeded");
        }

        // Validate file
        StorageUtils.validateFileType(file.getContentType());
        StorageUtils.validateFileSize(file.getSize());

        // Archive current version
        DocumentVersion version = new DocumentVersion();
        version.setDocument(currentDoc);
        version.setVersionNumber(currentDoc.getVersion());
        version.setFileName(currentDoc.getFileName());
        version.setMimeType(currentDoc.getMimeType());
        version.setFileSize(currentDoc.getFileSize());
        version.setStoragePath(currentDoc.getStoragePath());
        version.setChecksum(currentDoc.getChecksum());
        version.setArchivedAt(LocalDateTime.now());
        version.setArchivedBy(actor);
        documentVersionRepository.save(version);

        // Upload new file
        String newStoragePath = StorageUtils.generateVersionPath(currentDoc.getStoragePath(),
                currentDoc.getVersion() + 1);

        try {
            storageService.uploadFile(file, newStoragePath);
        } catch (Exception e) {
            log.error("Failed to upload new version: {}", e.getMessage());
            throw new BadRequestException("Failed to upload file");
        }

        // Calculate checksum
        String checksum = null;
        try {
            checksum = StorageUtils.calculateChecksum(file.getInputStream());
        } catch (IOException e) {
            log.warn("Failed to calculate checksum: {}", e.getMessage());
        }

        // Update document
        currentDoc.setStoragePath(newStoragePath);
        currentDoc.setFileSize(file.getSize());
        currentDoc.setMimeType(file.getContentType());
        currentDoc.setChecksum(checksum);
        currentDoc.setVersion(currentDoc.getVersion() + 1);
        currentDoc.setUpdatedAt(LocalDateTime.now());
        currentDoc.setStatus(DocumentStatus.DRAFT); // Reset to draft on new version
        currentDoc.setPreviousVersionId(version.getId());

        Document saved = documentRepository.save(currentDoc);

        // Update storage usage
        organization.addStorageUsage(sizeDiff);
        organizationRepository.save(organization);

        auditService.log("NEW_VERSION", "Document", saved.getId().toString(), actor.getId(), actor.getEmail(),
                "Uploaded version " + saved.getVersion() + " (" + StorageUtils.formatBytes(file.getSize()) + ")");

        return saved;
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Soft delete: move document to trash.
     */
    @Transactional
    public Document softDelete(Long id, User actor, Long organizationId) {
        Document doc = getById(id, organizationId, actor.getId());

        doc.setIsDeleted(true);
        doc.setDeletedAt(LocalDateTime.now());
        doc.setDeletedBy(actor);
        Document saved = documentRepository.save(doc);

        auditService.log("TRASH", "Document", id.toString(), actor.getId(), actor.getEmail(),
                "Moved to trash: " + doc.getFileName());

        return saved;
    }

    /**
     * Restore a soft-deleted document from trash.
     */
    @Transactional
    public Document restore(Long id, User actor, Long organizationId) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));

        if (!doc.getIsDeleted()) {
            throw new BadRequestException("Document is not in trash");
        }

        doc.setIsDeleted(false);
        doc.setDeletedAt(null);
        doc.setDeletedBy(null);
        Document saved = documentRepository.save(doc);

        auditService.log("RESTORE", "Document", id.toString(), actor.getId(), actor.getEmail(),
                "Restored from trash: " + doc.getFileName());

        return saved;
    }

    /**
     * Get documents in trash.
     */
    @Transactional(readOnly = true)
    public Page<Document> getTrash(Long organizationId, Pageable pageable, Long userId) {
        validateOrganizationAccess(organizationId, userId);
        return documentRepository.findAllByOrganizationIdAndIsDeletedTrue(organizationId, pageable);
    }

    /**
     * Permanently delete: remove from storage and database.
     */
    @Transactional
    public void permanentDelete(Long id, User actor, Long organizationId) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));

        if (doc.getOrganization() == null || !doc.getOrganization().getId().equals(organizationId)) {
            throw new EntityNotFoundException("Document not found in this organization: " + id);
        }

        // Delete from MinIO
        if (doc.getStoragePath() != null) {
            try {
                storageService.deleteFile(doc.getStoragePath());
            } catch (Exception e) {
                log.warn("Failed to delete file from storage: {}", e.getMessage());
            }
        }

        // Delete thumbnail if exists
        if (doc.getThumbnailPath() != null) {
            try {
                storageService.deleteFile(doc.getThumbnailPath());
            } catch (Exception e) {
                log.warn("Failed to delete thumbnail: {}", e.getMessage());
            }
        }

        // Update organization storage
        Organization org = doc.getOrganization();
        if (org != null) {
            org.removeStorageUsage(doc.getFileSize());
            organizationRepository.save(org);
        }

        // Update parent namespace stats
        if (doc.getParent() != null) {
            doc.getParent().removeDocument(doc.getFileSize());
            namespaceRepository.save(doc.getParent());
        }

        // Delete permissions
        permissionRepository.deleteByAppliesToId(id);

        // Delete document
        documentRepository.deleteById(id);

        auditService.log("PERMANENT_DELETE", "Document", id.toString(), actor.getId(), actor.getEmail(),
                "Permanently deleted document: " + doc.getFileName());
    }

    /**
     * Default delete: soft delete (move to trash).
     */
    @Transactional
    public void delete(Long id, User actor, Long organizationId) {
        softDelete(id, actor, organizationId);
    }

    // ==================== LISTING OPERATIONS ====================

    public Page<Document> getAll(Pageable pageable, DocumentStatus status, Long organizationId, Long userId) {
        validateOrganizationAccess(organizationId, userId);

        if (status != null) {
            return documentRepository.findAllByStatusAndOrganizationId(status, organizationId, pageable);
        }
        return documentRepository.findAllByOrganizationId(organizationId, pageable);
    }

    public Page<Document> getDocumentsByNamespace(Long namespaceId, Long organizationId, Pageable pageable,
            Long userId) {
        validateOrganizationAccess(organizationId, userId);
        return documentRepository.findByParentIdAndOrganizationId(namespaceId, organizationId, pageable);
    }

    public Page<Document> findAllByCreatedById(Long createdById, Long organizationId, Pageable pageable) {
        return documentRepository.findAllByCreatedByIdAndOrganizationId(createdById, organizationId, pageable);
    }

    public Page<Document> search(String fileName, String name, String mimeType,
            Long parentId, Long createdById, Long organizationId,
            Pageable pageable, Long userId) {
        validateOrganizationAccess(organizationId, userId);
        return documentRepository.search(fileName, name, mimeType, parentId, createdById, organizationId, pageable);
    }

    // ==================== WORKFLOW OPERATIONS ====================

    public Document submitForReview(Long id, User actor, Long organizationId) {
        Document doc = getById(id, organizationId, actor.getId());

        if (doc.getStatus() != DocumentStatus.DRAFT && doc.getStatus() != DocumentStatus.REJECTED) {
            throw new BadRequestException(
                    "Only DRAFT or REJECTED documents can be submitted for review. Current: " + doc.getStatus());
        }

        doc.setStatus(DocumentStatus.PENDING_REVIEW);
        doc.setUpdatedAt(LocalDateTime.now());
        Document saved = documentRepository.save(doc);

        auditService.log("SUBMIT_REVIEW", "Document", saved.getId().toString(),
                actor.getId(), actor.getEmail(), "Submitted for review");

        return saved;
    }

    public Document startReview(Long id, User actor, Long organizationId) {
        Document doc = getById(id, organizationId, actor.getId());

        if (doc.getStatus() != DocumentStatus.PENDING_REVIEW) {
            throw new BadRequestException("Document must be PENDING_REVIEW to start review");
        }

        doc.setStatus(DocumentStatus.IN_REVIEW);
        doc.setUpdatedAt(LocalDateTime.now());
        Document saved = documentRepository.save(doc);

        auditService.log("START_REVIEW", "Document", saved.getId().toString(),
                actor.getId(), actor.getEmail(), "Started review");

        return saved;
    }

    public Document approve(Long id, User actor, Long organizationId) {
        Document doc = getById(id, organizationId, actor.getId());

        if (doc.getStatus() != DocumentStatus.IN_REVIEW) {
            throw new BadRequestException("Document must be IN_REVIEW to be approved");
        }

        doc.setStatus(DocumentStatus.APPROVED);
        doc.setUpdatedAt(LocalDateTime.now());
        Document saved = documentRepository.save(doc);

        auditService.log("APPROVE", "Document", saved.getId().toString(),
                actor.getId(), actor.getEmail(), "Approved document");

        return saved;
    }

    public Document reject(Long id, String reason, User actor, Long organizationId) {
        Document doc = getById(id, organizationId, actor.getId());

        if (doc.getStatus() != DocumentStatus.IN_REVIEW && doc.getStatus() != DocumentStatus.PENDING_REVIEW) {
            throw new BadRequestException("Document must be in review to be rejected");
        }

        doc.setStatus(DocumentStatus.REJECTED);
        doc.setUpdatedAt(LocalDateTime.now());
        Document saved = documentRepository.save(doc);

        auditService.log("REJECT", "Document", saved.getId().toString(),
                actor.getId(), actor.getEmail(), "Rejected: " + reason);

        return saved;
    }

    public Document publish(Long id, User actor, Long organizationId) {
        Document doc = getById(id, organizationId, actor.getId());

        if (doc.getStatus() != DocumentStatus.APPROVED) {
            throw new BadRequestException("Document must be APPROVED to be published");
        }

        doc.setStatus(DocumentStatus.PUBLISHED);
        doc.setUpdatedAt(LocalDateTime.now());
        Document saved = documentRepository.save(doc);

        auditService.log("PUBLISH", "Document", saved.getId().toString(),
                actor.getId(), actor.getEmail(), "Published document");

        return saved;
    }

    public Document archive(Long id, User actor, Long organizationId) {
        Document doc = getById(id, organizationId, actor.getId());

        doc.setStatus(DocumentStatus.ARCHIVED);
        doc.setUpdatedAt(LocalDateTime.now());
        Document saved = documentRepository.save(doc);

        auditService.log("ARCHIVE", "Document", saved.getId().toString(),
                actor.getId(), actor.getEmail(), "Archived document");

        return saved;
    }

    // ==================== HELPER METHODS ====================

    private void validateOrganizationAccess(Long organizationId, Long userId) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required");
        }
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }
        // Additional access checks can be added here
    }
}
