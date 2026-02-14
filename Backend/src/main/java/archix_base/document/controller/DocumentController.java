package archix_base.document.controller;

import archix_base.common.response.PageResponse;
import archix_base.document.dto.DocumentDto;
import archix_base.document.entity.Document;
import archix_base.document.entity.DocumentStatus;
import archix_base.document.mapper.DocumentMapper;
import archix_base.document.service.DocumentService;
import archix_base.identity.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST Controller for document management with MinIO storage.
 * Provides CRUD operations, versioning, workflow management, and search.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documents", description = "Document management API with MinIO storage")
public class DocumentController {

        private final DocumentService documentService;

        // ==================== CRUD OPERATIONS ====================

        /**
         * GET /api/documents - List all documents with pagination.
         */
        @GetMapping
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER', 'READER')")
        @Operation(summary = "List documents", description = "Get all documents with pagination and filtering")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<PageResponse<DocumentDto>> getAll(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir,
                        @Parameter(description = "Filter by document status") @RequestParam(required = false) DocumentStatus status,
                        @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/documents - Org: {}, User: {}", organizationId, currentUser.getEmail());

                // If no organization header, get user's organization
                if (organizationId == null && currentUser.getOrganization() != null) {
                        organizationId = currentUser.getOrganization().getId();
                }

                if (organizationId == null) {
                        return ResponseEntity.ok(PageResponse.empty());
                }

                Sort sort = sortDir.equalsIgnoreCase("asc")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<DocumentDto> result = documentService
                                .getAll(pageable, status, organizationId, currentUser.getId())
                                .map(DocumentMapper::toDto);

                return ResponseEntity.ok(PageResponse.of(result));
        }

        /**
         * GET /api/documents/{id} - Get document by ID.
         */
        @GetMapping("/{id}")
        @PreAuthorize("@accessControlService.canView(#id, authentication)")
        @Operation(summary = "Get document", description = "Get document metadata by ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Document found"),
                        @ApiResponse(responseCode = "404", description = "Document not found")
        })
        public ResponseEntity<DocumentDto> getById(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Document doc = documentService.getById(id, organizationId, currentUser.getId());
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        /**
         * GET /api/documents/{id}/content - Download document content (streaming).
         */
        @GetMapping("/{id}/content")
        @PreAuthorize("@accessControlService.canView(#id, authentication)")
        @Operation(summary = "Download document", description = "Download document content with streaming")
        public ResponseEntity<Resource> getContent(
                        @PathVariable Long id,
                        @Parameter(description = "Force download instead of inline display") @RequestParam(defaultValue = "false") boolean download,
                        @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.info("GET /api/documents/{}/content - Download: {}", id, download);

                Long effectiveOrgId = resolveOrganizationId(organizationId, currentUser);
                if (effectiveOrgId == null) {
                        // Fallback: derive organization from the document itself
                        try {
                                Document fallbackDoc = documentService.getByIdWithoutOrgCheck(id);
                                if (fallbackDoc != null && fallbackDoc.getOrganization() != null) {
                                        effectiveOrgId = fallbackDoc.getOrganization().getId();
                                }
                        } catch (Exception e) {
                                log.warn("Could not derive org from document {}: {}", id, e.getMessage());
                        }
                        if (effectiveOrgId == null) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                        }
                }

                try {
                        Document doc = documentService.getById(id, effectiveOrgId, currentUser.getId());
                        Resource resource = documentService.getDocumentContentAsResource(id, effectiveOrgId,
                                        currentUser.getId());

                        String disposition = download ? "attachment" : "inline";
                        String mimeType = doc.getMimeType() != null ? doc.getMimeType() : "application/octet-stream";
                        String fileName = doc.getFileName() != null ? doc.getFileName() : "document-" + id;

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        disposition + "; filename=\"" + fileName + "\"")
                                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(doc.getFileSize()))
                                        .contentType(MediaType.parseMediaType(mimeType))
                                        .body(resource);
                } catch (Exception e) {
                        log.warn("Document content not available for {}: {}", id, e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
        }

        /**
         * GET /api/documents/{id}/download-url - Get presigned download URL.
         */
        @GetMapping("/{id}/download-url")
        @PreAuthorize("@accessControlService.canView(#id, authentication)")
        @Operation(summary = "Get download URL", description = "Generate a presigned URL for direct download")
        public ResponseEntity<Map<String, String>> getDownloadUrl(
                        @PathVariable Long id,
                        @Parameter(description = "URL expiration time in minutes (default: 60)") @RequestParam(defaultValue = "60") int expirationMinutes,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                String url = documentService.generateDownloadUrl(id, organizationId, currentUser.getId(),
                                expirationMinutes);
                return ResponseEntity.ok(Map.of(
                                "url", url,
                                "expiresIn", String.valueOf(expirationMinutes * 60),
                                "documentId", id.toString()));
        }

        /**
         * POST /api/documents/upload - Upload a new document.
         */
        @PostMapping("/upload")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER')")
        @Operation(summary = "Upload document", description = "Upload a new document to MinIO storage")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid file or quota exceeded"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<DocumentDto> uploadFile(
                        @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file,
                        @Parameter(description = "Document name (default: original filename)") @RequestParam(value = "name", required = false) String name,
                        @Parameter(description = "Parent namespace ID") @RequestParam(value = "parentId", required = false) Long parentId,
                        @Parameter(description = "Namespace ID (alias for parentId)") @RequestParam(value = "namespaceId", required = false) Long namespaceId,
                        @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                // Use user's organization if header not provided
                Long effectiveOrgId = organizationId;
                if (effectiveOrgId == null && currentUser.getOrganization() != null) {
                        effectiveOrgId = currentUser.getOrganization().getId();
                }
                if (effectiveOrgId == null && currentUser.getDepartment() != null
                                && currentUser.getDepartment().getOrganization() != null) {
                        effectiveOrgId = currentUser.getDepartment().getOrganization().getId();
                }

                log.info("POST /api/documents/upload - File: {}, Org: {}, User: {}",
                                file.getOriginalFilename(), effectiveOrgId, currentUser.getEmail());

                Long finalParentId = parentId != null ? parentId : namespaceId;
                String documentName = name != null && !name.isEmpty() ? name : file.getOriginalFilename();

                Document saved = documentService.create(file, documentName, finalParentId,
                                currentUser.getId(), effectiveOrgId);

                return ResponseEntity.status(HttpStatus.CREATED).body(DocumentMapper.toDto(saved));
        }

        /**
         * PUT /api/documents/{id} - Update document metadata.
         */
        @PutMapping("/{id}")
        @PreAuthorize("@accessControlService.canEdit(#id, authentication)")
        @Operation(summary = "Update document", description = "Update document metadata")
        public ResponseEntity<DocumentDto> updateMetadata(
                        @PathVariable Long id,
                        @RequestBody Map<String, Object> updates,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.info("PUT /api/documents/{} - Updating metadata", id);

                String name = (String) updates.get("name");
                String description = (String) updates.get("description");
                String tags = (String) updates.get("tags");
                Long parentId = updates.get("parentId") != null ? Long.valueOf(updates.get("parentId").toString())
                                : null;

                Document updated = documentService.updateMetadata(id, name, description, tags,
                                parentId, currentUser, organizationId);

                return ResponseEntity.ok(DocumentMapper.toDto(updated));
        }

        /**
         * POST /api/documents/{id}/upload-version - Upload new version.
         */
        @PostMapping("/{id}/upload-version")
        @PreAuthorize("@accessControlService.canEdit(#id, authentication)")
        @Operation(summary = "Upload new version", description = "Upload a new version of an existing document")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "New version uploaded"),
                        @ApiResponse(responseCode = "400", description = "Invalid file"),
                        @ApiResponse(responseCode = "404", description = "Document not found")
        })
        public ResponseEntity<DocumentDto> uploadNewVersion(
                        @PathVariable Long id,
                        @RequestParam("file") MultipartFile file,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.info("POST /api/documents/{}/upload-version - File: {}", id, file.getOriginalFilename());

                Document updated = documentService.uploadNewVersion(id, file, currentUser, organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(updated));
        }

        /**
         * DELETE /api/documents/{id} - Delete document.
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("@accessControlService.canDelete(#id, authentication)")
        @Operation(summary = "Delete document", description = "Delete a document from storage")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Document deleted"),
                        @ApiResponse(responseCode = "404", description = "Document not found")
        })
        public ResponseEntity<Void> delete(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.info("DELETE /api/documents/{}", id);

                documentService.delete(id, currentUser, organizationId);
                return ResponseEntity.noContent().build();
        }

        // ==================== WORKFLOW OPERATIONS ====================

        @PostMapping("/{id}/submit")
        @PreAuthorize("@accessControlService.canEdit(#id, authentication)")
        @Operation(summary = "Submit for review", description = "Submit document for approval workflow")
        public ResponseEntity<DocumentDto> submitForReview(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Document doc = documentService.submitForReview(id, currentUser, organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        @PostMapping("/{id}/start-review")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
        @Operation(summary = "Start review", description = "Begin reviewing a submitted document")
        public ResponseEntity<DocumentDto> startReview(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Document doc = documentService.startReview(id, currentUser, organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        @PostMapping("/{id}/approve")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
        @Operation(summary = "Approve document", description = "Approve a document in review")
        public ResponseEntity<DocumentDto> approve(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Document doc = documentService.approve(id, currentUser, organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        @PostMapping("/{id}/reject")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
        @Operation(summary = "Reject document", description = "Reject a document in review with a reason")
        public ResponseEntity<DocumentDto> reject(
                        @PathVariable Long id,
                        @RequestBody Map<String, String> payload,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                String reason = payload.getOrDefault("reason", "No reason provided");
                Document doc = documentService.reject(id, reason, currentUser, organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        @PostMapping("/{id}/publish")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
        @Operation(summary = "Publish document", description = "Publish an approved document")
        public ResponseEntity<DocumentDto> publish(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Document doc = documentService.publish(id, currentUser, organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        @PostMapping("/{id}/archive")
        @PreAuthorize("@accessControlService.canAdmin(#id, authentication)")
        @Operation(summary = "Archive document", description = "Archive a document")
        public ResponseEntity<DocumentDto> archive(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Document doc = documentService.archive(id, currentUser, organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        // ==================== LISTING & SEARCH ====================

        /**
         * GET /api/documents/trash - Get documents in trash.
         */
        @GetMapping("/trash")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER')")
        @Operation(summary = "Get trash", description = "List all soft-deleted documents")
        public ResponseEntity<PageResponse<DocumentDto>> getTrash(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
                Page<DocumentDto> result = documentService
                                .getTrash(organizationId, pageable, currentUser.getId())
                                .map(DocumentMapper::toDto);
                return ResponseEntity.ok(PageResponse.of(result));
        }

        /**
         * POST /api/documents/{id}/restore - Restore from trash.
         */
        @PostMapping("/{id}/restore")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER')")
        @Operation(summary = "Restore document", description = "Restore a document from trash")
        public ResponseEntity<DocumentDto> restore(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Document doc = documentService.restore(id, currentUser, organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        /**
         * DELETE /api/documents/{id}/permanent - Permanently delete.
         */
        @DeleteMapping("/{id}/permanent")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
        @Operation(summary = "Permanent delete", description = "Permanently delete a document from storage")
        public ResponseEntity<Void> permanentDelete(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                documentService.permanentDelete(id, currentUser, organizationId);
                return ResponseEntity.noContent().build();
        }

        /**
         * GET /api/documents/namespace/{namespaceId} - Documents in namespace.
         */
        @GetMapping("/namespace/{namespaceId}")
        @PreAuthorize("@accessControlService.canView(#namespaceId, authentication)")
        @Operation(summary = "Get documents by namespace", description = "List all documents in a namespace")
        public ResponseEntity<PageResponse<DocumentDto>> getByNamespace(
                        @PathVariable Long namespaceId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<DocumentDto> result = documentService
                                .getDocumentsByNamespace(namespaceId, organizationId, pageable, currentUser.getId())
                                .map(DocumentMapper::toDto);

                return ResponseEntity.ok(PageResponse.of(result));
        }

        /**
         * GET /api/documents/my - Current user's documents.
         */
        @GetMapping("/my")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get my documents", description = "List all documents created by the current user")
        public ResponseEntity<PageResponse<DocumentDto>> getMyDocuments(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<DocumentDto> result = documentService
                                .findAllByCreatedById(currentUser.getId(), organizationId, pageable)
                                .map(DocumentMapper::toDto);

                return ResponseEntity.ok(PageResponse.of(result));
        }

        /**
         * GET /api/documents/search - Advanced search with filters.
         */
        @GetMapping("/search")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER', 'READER')")
        @Operation(summary = "Search documents", description = "Advanced search with multiple filters")
        public ResponseEntity<PageResponse<DocumentDto>> search(
                        @Parameter(description = "Filter by filename") @RequestParam(required = false) String fileName,
                        @Parameter(description = "Filter by document name") @RequestParam(required = false) String name,
                        @Parameter(description = "Filter by MIME type") @RequestParam(required = false) String mimeType,
                        @Parameter(description = "Filter by parent namespace ID") @RequestParam(required = false) Long parentId,
                        @Parameter(description = "Filter by creator user ID") @RequestParam(required = false) Long createdById,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/documents/search - Query: name={}, fileName={}", name, fileName);

                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<DocumentDto> result = documentService
                                .search(fileName, name, mimeType, parentId, createdById, organizationId,
                                                pageable, currentUser.getId())
                                .map(DocumentMapper::toDto);

                return ResponseEntity.ok(PageResponse.of(result));
        }

        // ==================== VERSION MANAGEMENT ====================

        /**
         * GET /api/documents/{id}/versions - Get all versions of a document.
         */
        @GetMapping("/{id}/versions")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER', 'READER')")
        @Operation(summary = "Get document versions", description = "Get all versions of a document")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Versions retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Document not found")
        })
        public ResponseEntity<?> getVersions(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/documents/{}/versions", id);

                try {
                        var versions = documentService.getVersions(id, organizationId, currentUser.getId());
                        return ResponseEntity.ok(versions);
                } catch (Exception e) {
                        log.warn("No versions found for document {}: {}", id, e.getMessage());
                        // Return empty list if no versions (document is version 1)
                        return ResponseEntity.ok(java.util.Collections.emptyList());
                }
        }

        /**
         * GET /api/documents/{id}/content/thumbnail - Get document thumbnail/preview.
         * Returns a placeholder for documents without thumbnail support.
         */
        @GetMapping("/{id}/content/thumbnail")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER', 'READER')")
        @Operation(summary = "Get document thumbnail", description = "Get a thumbnail preview of the document")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Thumbnail retrieved"),
                        @ApiResponse(responseCode = "404", description = "Document or thumbnail not found")
        })
        public ResponseEntity<?> getThumbnail(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/documents/{}/content/thumbnail", id);

                Long effectiveOrgId = resolveOrganizationId(organizationId, currentUser);
                if (effectiveOrgId == null) {
                        return ResponseEntity.ok(java.util.Map.of(
                                        "available", false,
                                        "message", "No organization context available"));
                }

                try {
                        // For now, return document info - thumbnail generation is not implemented yet
                        Document doc = documentService.getById(id, effectiveOrgId, currentUser.getId());

                        // Return info that thumbnail is not available (let frontend handle it)
                        return ResponseEntity.ok(java.util.Map.of(
                                        "available", false,
                                        "mimeType",
                                        doc.getMimeType() != null ? doc.getMimeType() : "application/octet-stream",
                                        "message", "Thumbnail preview not available for this document type"));
                } catch (Exception e) {
                        log.warn("Failed to get thumbnail for document {}: {}", id, e.getMessage());
                        return ResponseEntity.ok(java.util.Map.of(
                                        "available", false,
                                        "message", "Thumbnail not available"));
                }
        }

        // ==================== HELPER METHODS ====================

        /**
         * Resolves the organization ID from header or user context.
         */
        private Long resolveOrganizationId(Long headerOrgId, User currentUser) {
                if (headerOrgId != null) {
                        return headerOrgId;
                }
                try {
                        if (currentUser.getOrganization() != null) {
                                return currentUser.getOrganization().getId();
                        }
                } catch (Exception e) {
                        log.debug("Could not resolve org from user.organization: {}", e.getMessage());
                }
                try {
                        if (currentUser.getDepartment() != null
                                        && currentUser.getDepartment().getOrganization() != null) {
                                return currentUser.getDepartment().getOrganization().getId();
                        }
                } catch (Exception e) {
                        log.debug("Could not resolve org from user.department: {}", e.getMessage());
                }
                return null;
        }
}
