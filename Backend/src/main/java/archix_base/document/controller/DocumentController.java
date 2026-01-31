package archix_base.document.controller;

import archix_base.common.response.PageResponse;
import archix_base.document.dto.DocumentDto;
import archix_base.document.dto.DocumentStatsDTO;
import archix_base.document.entity.Document;
import archix_base.document.mapper.DocumentMapper;
import archix_base.document.service.DocumentService;
import archix_base.identity.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

        private final DocumentService documentService;

        // --- Workflow Endpoints ---

        @PostMapping("/{id}/submit")
        public ResponseEntity<DocumentDto> submitForReview(
                        @PathVariable Long id,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                return ResponseEntity.ok(
                                DocumentMapper.toDto(documentService.submitForReview(id, currentUser, organizationId)));
        }

        @PostMapping("/{id}/start-review")
        public ResponseEntity<DocumentDto> startReview(
                        @PathVariable Long id,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                return ResponseEntity
                                .ok(DocumentMapper.toDto(documentService.startReview(id, currentUser, organizationId)));
        }

        @PostMapping("/{id}/approve")
        public ResponseEntity<DocumentDto> approve(
                        @PathVariable Long id,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                return ResponseEntity
                                .ok(DocumentMapper.toDto(documentService.approve(id, currentUser, organizationId)));
        }

        @PostMapping("/{id}/reject")
        public ResponseEntity<DocumentDto> reject(
                        @PathVariable Long id,
                        @RequestBody Map<String, String> payload,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                String reason = payload.get("reason");
                return ResponseEntity.ok(
                                DocumentMapper.toDto(documentService.reject(id, reason, currentUser, organizationId)));
        }

        @PostMapping("/{id}/publish")
        public ResponseEntity<DocumentDto> publish(
                        @PathVariable Long id,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                return ResponseEntity
                                .ok(DocumentMapper.toDto(documentService.publish(id, currentUser, organizationId)));
        }

        @PostMapping("/{id}/archive")
        public ResponseEntity<DocumentDto> archive(
                        @PathVariable Long id,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                return ResponseEntity
                                .ok(DocumentMapper.toDto(documentService.archive(id, currentUser, organizationId)));
        }

        @GetMapping("/{id}/versions")
        public ResponseEntity<List<archix_base.document.dto.DocumentVersionDto>> getVersions(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {
                return ResponseEntity.ok(documentService.getVersionDtos(id, organizationId, currentUser.getId()));
        }

        @GetMapping("/stats")
        public ResponseEntity<DocumentStatsDTO> getStats(
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {
                return ResponseEntity.ok(documentService.getStats(organizationId, currentUser.getId()));
        }

        @GetMapping
        public ResponseEntity<PageResponse<DocumentDto>> getAll(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir,
                        @RequestParam(required = false) archix_base.document.entity.DocumentStatus status,
                        @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
                        @AuthenticationPrincipal User currentUser) {
                try {
                        if (organizationId == null) {
                                // Fallback or Error? Ideally Error to enforce tenancy
                                System.err.println("MISSING ORG ID for getAll");
                                return ResponseEntity.badRequest().build();
                        }

                        Sort sort = sortDir.equalsIgnoreCase("asc")
                                        ? Sort.by(sortBy).ascending()
                                        : Sort.by(sortBy).descending();
                        Pageable pageable = PageRequest.of(page, size, sort);

                        Page<DocumentDto> result = documentService
                                        .getAll(pageable, status, organizationId, currentUser.getId())
                                        .map(DocumentMapper::toDto);

                        return ResponseEntity.ok(PageResponse.of(result));
                } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                }
        }

        @GetMapping("/{id}")
        public ResponseEntity<DocumentDto> getById(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {
                Document doc = documentService.getById(id, organizationId, currentUser.getId());
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        @GetMapping("/{id}/content")
        public ResponseEntity<byte[]> getContent(
                        @PathVariable Long id,
                        @RequestParam(defaultValue = "false") boolean download,
                        @RequestHeader(value = "X-Organization-ID", required = false) Long headerOrgId,
                        @RequestParam(value = "organizationId", required = false) Long paramOrgId,
                        @AuthenticationPrincipal User currentUser) {
                try {
                        Long organizationId = paramOrgId != null ? paramOrgId : headerOrgId;
                        if (organizationId == null) {
                                return ResponseEntity.badRequest().build();
                        }

                        System.out.println("GET CONTENT REQUEST: ID=" + id + " Org=" + organizationId);
                        Document doc = documentService.getById(id, organizationId, currentUser.getId());
                        byte[] content = documentService.getDocumentContent(id, organizationId, currentUser.getId());

                        String disposition = download ? "attachment" : "inline";
                        String mimeType = doc.getMimeType() != null ? doc.getMimeType() : "application/octet-stream";
                        String fileName = doc.getFileName() != null ? doc.getFileName() : "document-" + id;

                        return ResponseEntity.ok()
                                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                                        disposition + "; filename=\"" + fileName + "\"")
                                        .contentType(org.springframework.http.MediaType.parseMediaType(mimeType))
                                        .body(content);
                } catch (Exception e) {
                        System.err.println("ERROR IN GET CONTENT for ID " + id + ":");
                        e.printStackTrace();
                        return ResponseEntity.internalServerError().build();
                }
        }

        @PostMapping
        public ResponseEntity<DocumentDto> create(
                        @Valid @RequestBody DocumentDto dto,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                // Utiliser l'utilisateur connectÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â© si
                // createdById n'est pas fourni
                Long createdById = dto.getCreatedById() != null ? dto.getCreatedById() : currentUser.getId();

                dto.setId(null);
                Document doc = DocumentMapper.toEntity(dto);
                Document saved = documentService.create(doc, createdById, dto.getParentId(), organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(saved));
        }

        /**
         * POST /api/documents/upload
         * Upload d'un fichier
         */
        @PostMapping("/upload")
        public ResponseEntity<?> uploadFile(
                        @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                        @RequestParam(value = "parentId", required = false) Long parentId,
                        @RequestParam(value = "namespaceId", required = false) Long namespaceId,
                        @RequestParam(value = "title", required = false) String title,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                try {
                        if (currentUser == null) {
                                return ResponseEntity.status(401).build();
                        }

                        // Créer un document
                        Document doc = new Document();
                        doc.setName(title != null && !title.isEmpty() ? title : file.getOriginalFilename());
                        doc.setFileName(file.getOriginalFilename());
                        doc.setMimeType(file.getContentType());
                        doc.setSize(file.getSize());
                        doc.setContent(file.getBytes());

                        Long finalParentId = parentId != null ? parentId : namespaceId;
                        Document saved = documentService.create(doc, currentUser.getId(), finalParentId,
                                        organizationId);
                        return ResponseEntity.ok(DocumentMapper.toDto(saved));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Erreur upload: " + e.getMessage(), e);
                }
        }

        @PutMapping("/{id}")
        public ResponseEntity<DocumentDto> update(
                        @PathVariable Long id,
                        @Valid @RequestBody DocumentDto dto,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                Document doc = DocumentMapper.toEntity(dto);
                Document updated = documentService.update(id, doc, dto.getParentId(), currentUser, organizationId);
                return ResponseEntity.ok(DocumentMapper.toDto(updated));
        }

        /**
         * DELETE /api/documents/{id}
         * Supprimer un document
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id,
                        @AuthenticationPrincipal User currentUser,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                documentService.delete(id, currentUser, organizationId);
                return ResponseEntity.noContent().build();
        }

        /**
         * GET /api/documents/namespace/{namespaceId}
         * Documents d'un namespace avec pagination
         */
        @GetMapping("/namespace/{namespaceId}")
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
         * GET /api/documents/my
         * Documents de l'utilisateur connectÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©
         */
        @GetMapping("/my")
        public ResponseEntity<PageResponse<DocumentDto>> getMyDocuments(
                        @AuthenticationPrincipal User currentUser,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                // Use SEARCH to filter by org and user
                Page<DocumentDto> result = documentService
                                .search(null, null, null, null, currentUser.getId(), organizationId, pageable,
                                                currentUser.getId())
                                .map(DocumentMapper::toDto);
                return ResponseEntity.ok(PageResponse.of(result));
        }

        /**
         * GET /api/documents/search
         * Recherche avancÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©e avec pagination
         */
        @GetMapping("/search")
        public ResponseEntity<PageResponse<DocumentDto>> search(
                        @RequestParam(required = false) String fileName,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String mimeType,
                        @RequestParam(required = false) Long parentId,
                        @RequestParam(required = false) Long createdById,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestHeader(value = "X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<DocumentDto> result = documentService
                                .search(fileName, name, mimeType, parentId, createdById, organizationId, pageable,
                                                currentUser.getId())
                                .map(DocumentMapper::toDto);
                return ResponseEntity.ok(PageResponse.of(result));
        }
}
