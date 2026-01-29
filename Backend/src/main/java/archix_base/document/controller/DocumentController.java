package archix_base.document.controller;

import archix_base.common.response.PageResponse;
import archix_base.document.dto.DocumentDto;
import archix_base.document.entity.Document;
import archix_base.document.mapper.DocumentMapper;
import archix_base.document.service.DocumentService;
import archix_base.identity.entity.User;
import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

//package archix_base.document.controller;

//
//import archix_base.document.dto.DocumentDto;
//import archix_base.document.entity.Document;
//import archix_base.document.mapper.DocumentMapper;
//import archix_base.document.service.DocumentService;
//import archix_base.common.exception.EntityNotFoundException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/documents")
//public class DocumentController {
//
//    @Autowired
//    private DocumentService documentService;
//
//    @GetMapping
//    public List<DocumentDto> getAll() {
//        return documentService.getAll().stream()
//                .map(DocumentMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @GetMapping("/{id}")
//    public DocumentDto getById(@PathVariable Long id) {
//        Document doc = documentService.getById(id);
//        return DocumentMapper.toDto(doc);
//    }
//
//    @PostMapping
//    public DocumentDto create(@RequestBody DocumentDto dto) {
//        dto.setId(null);
//        Document doc = DocumentMapper.toEntity(dto);
//        Document saved = documentService.create(doc, dto.getCreatedById(), dto.getParentId());
//        return DocumentMapper.toDto(saved);
//    }
//
//    @PutMapping("/{id}")
//    public DocumentDto update(@PathVariable Long id, @RequestBody DocumentDto dto) {
//        Document doc = DocumentMapper.toEntity(dto);
//        Document updated = documentService.update(id, doc, dto.getParentId());
//        return DocumentMapper.toDto(updated);
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Long id) {
//        documentService.delete(id);
//    }
//
//    // Endpoint pour lister les documents d'un namespace parent
//    @GetMapping("/namespace/{namespaceId}")
//    public List<DocumentDto> getByNamespace(@PathVariable Long namespaceId) {
//        return documentService.getDocumentsByNamespace(namespaceId)
//                .stream()
//                .map(DocumentMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @GetMapping("/by-creator/{userId}")
//    public List<DocumentDto> getByCreator(@PathVariable Long userId) {
//        return documentService.findAllByCreatedById(userId).stream()
//                .map(DocumentMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    // GET /api/documents/search
//    @GetMapping("/search")
//    public List<DocumentDto> searchDocuments(
//            @RequestParam(required = false) String fileName,
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String mimeType,
//            @RequestParam(required = false) Long parentId,
//            @RequestParam(required = false) Long createdById
//    ) {
//        return documentService.advancedSearch(fileName, name, mimeType, parentId, createdById)
//                .stream()
//                .map(DocumentMapper::toDto)
//                .collect(Collectors.toList());
//    }
//}

import archix_base.document.dto.DocumentStatsDTO;

// ... existing imports ...

import archix_base.document.dto.DocumentStatsDTO;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

        private final DocumentService documentService;

        // --- Workflow Endpoints ---

        @PostMapping("/{id}/submit")
        public ResponseEntity<DocumentDto> submitForReview(@PathVariable Long id,
                        @AuthenticationPrincipal User currentUser) {
                return ResponseEntity.ok(DocumentMapper.toDto(documentService.submitForReview(id, currentUser)));
        }

        @PostMapping("/{id}/start-review")
        public ResponseEntity<DocumentDto> startReview(@PathVariable Long id,
                        @AuthenticationPrincipal User currentUser) {
                return ResponseEntity.ok(DocumentMapper.toDto(documentService.startReview(id, currentUser)));
        }

        @PostMapping("/{id}/approve")
        public ResponseEntity<DocumentDto> approve(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
                return ResponseEntity.ok(DocumentMapper.toDto(documentService.approve(id, currentUser)));
        }

        @PostMapping("/{id}/reject")
        public ResponseEntity<DocumentDto> reject(@PathVariable Long id, @RequestBody Map<String, String> payload,
                        @AuthenticationPrincipal User currentUser) {
                String reason = payload.get("reason");
                return ResponseEntity.ok(DocumentMapper.toDto(documentService.reject(id, reason, currentUser)));
        }

        @PostMapping("/{id}/publish")
        public ResponseEntity<DocumentDto> publish(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
                return ResponseEntity.ok(DocumentMapper.toDto(documentService.publish(id, currentUser)));
        }

        @PostMapping("/{id}/archive")
        public ResponseEntity<DocumentDto> archive(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
                return ResponseEntity.ok(DocumentMapper.toDto(documentService.archive(id, currentUser)));
        }

        @GetMapping("/{id}/versions")
        public ResponseEntity<List<archix_base.document.dto.DocumentVersionDto>> getVersions(@PathVariable Long id) {
                return ResponseEntity.ok(documentService.getVersions(id).stream()
                                .map(DocumentMapper::toVersionDto)
                                .collect(Collectors.toList()));
        }

        @GetMapping("/stats")
        public ResponseEntity<DocumentStatsDTO> getStats() {
                return ResponseEntity.ok(documentService.getStats());
        }

        @GetMapping
        public ResponseEntity<PageResponse<DocumentDto>> getAll(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir,
                        @RequestParam(required = false) archix_base.document.entity.DocumentStatus status) {
                Sort sort = sortDir.equalsIgnoreCase("asc")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<DocumentDto> result = documentService.getAll(pageable, status)
                                .map(DocumentMapper::toDto);

                return ResponseEntity.ok(PageResponse.of(result));
        }

        @GetMapping("/{id}")
        public ResponseEntity<DocumentDto> getById(@PathVariable Long id) {
                Document doc = documentService.getById(id);
                return ResponseEntity.ok(DocumentMapper.toDto(doc));
        }

        @GetMapping("/{id}/content")
        public ResponseEntity<byte[]> getContent(@PathVariable Long id,
                        @RequestParam(defaultValue = "false") boolean download) {
                Document doc = documentService.getById(id);
                byte[] content = documentService.getDocumentContent(id);

                String disposition = download ? "attachment" : "inline";

                return ResponseEntity.ok()
                                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                                disposition + "; filename=\"" + doc.getFileName() + "\"")
                                .contentType(org.springframework.http.MediaType.parseMediaType(doc.getMimeType()))
                                .body(content);
        }

        @PostMapping
        public ResponseEntity<DocumentDto> create(
                        @Valid @RequestBody DocumentDto dto,
                        @AuthenticationPrincipal User currentUser) {
                // Utiliser l'utilisateur connectÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â© si
                // createdById n'est pas fourni
                Long createdById = dto.getCreatedById() != null ? dto.getCreatedById() : currentUser.getId();

                dto.setId(null);
                Document doc = DocumentMapper.toEntity(dto);
                Document saved = documentService.create(doc, createdById, dto.getParentId());
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
                        @AuthenticationPrincipal User currentUser) {
                try {
                        if (currentUser == null) {
                                return ResponseEntity.status(401).build();
                        }
                        System.out.println("Uploading file: " + file.getOriginalFilename());
                        System.out.println("Parent ID received: " + parentId);
                        System.out.println("Namespace ID received: " + namespaceId);

                        // Créer un document
                        Document doc = new Document();
                        doc.setName(title != null && !title.isEmpty() ? title : file.getOriginalFilename());
                        doc.setFileName(file.getOriginalFilename());
                        doc.setMimeType(file.getContentType());
                        doc.setSize(file.getSize());
                        doc.setContent(file.getBytes());

                        Long finalParentId = parentId != null ? parentId : namespaceId;
                        Document saved = documentService.create(doc, currentUser.getId(), finalParentId);
                        return ResponseEntity.ok(DocumentMapper.toDto(saved));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                } catch (Exception e) {
                        e.printStackTrace();
                        // Return error message in body so frontend can display it or user can inspect
                        // network tab
                        throw new RuntimeException("Erreur upload: " + e.getMessage(), e);
                }
        }

        @PutMapping("/{id}")
        public ResponseEntity<DocumentDto> update(
                        @PathVariable Long id,
                        @Valid @RequestBody DocumentDto dto,
                        @AuthenticationPrincipal User currentUser) {
                Document doc = DocumentMapper.toEntity(dto);
                Document updated = documentService.update(id, doc, dto.getParentId(), currentUser);
                return ResponseEntity.ok(DocumentMapper.toDto(updated));
        }

        /**
         * DELETE /api/documents/{id}
         * Supprimer un document
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
                documentService.delete(id, currentUser);
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
                        @RequestParam(defaultValue = "20") int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<DocumentDto> result = documentService.getDocumentsByNamespace(namespaceId, pageable)
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
                        @RequestParam(defaultValue = "20") int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<DocumentDto> result = documentService.findAllByCreatedById(currentUser.getId(), pageable)
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
                        @RequestParam(defaultValue = "20") int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<DocumentDto> result = documentService
                                .search(fileName, name, mimeType, parentId, createdById, pageable)
                                .map(DocumentMapper::toDto);
                return ResponseEntity.ok(PageResponse.of(result));
        }
}
