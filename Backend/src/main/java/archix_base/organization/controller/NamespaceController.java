package archix_base.organization.controller;

import archix_base.common.exception.EntityNotFoundException;
import archix_base.document.dto.DocumentDto;
import archix_base.document.mapper.DocumentMapper;
import archix_base.document.service.DocumentService;
import archix_base.identity.entity.User;
import archix_base.organization.dto.NamespaceDto;
import archix_base.organization.entity.Namespace;
import archix_base.organization.mapper.NamespaceMapper;
import archix_base.organization.service.NamespaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API controller for managing namespaces (folder-like hierarchy).
 * Provides CRUD operations for organizing documents in a tree structure.
 */
@RestController
@RequestMapping("/api/namespaces")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Namespaces", description = "Namespace management API for organizing documents")
public class NamespaceController {

        private final NamespaceService namespaceService;
        private final DocumentService documentService;

        // ==================== LIST OPERATIONS ====================

        /**
         * GET /api/namespaces - List all namespaces for organization.
         */
        @GetMapping
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER', 'READER')")
        @Operation(summary = "List all namespaces", description = "Get all namespaces in the organization")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Namespaces retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<List<NamespaceDto>> getAll(
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/namespaces - Org: {}, User: {}", organizationId, currentUser.getEmail());

                List<NamespaceDto> namespaces = namespaceService.advancedSearch(null, null, null, organizationId)
                                .stream()
                                .map(NamespaceMapper::toDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(namespaces);
        }

        /**
         * GET /api/namespaces/roots - Get root namespaces.
         */
        @GetMapping("/roots")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER', 'READER')")
        @Operation(summary = "Get root namespaces", description = "Get all top-level namespaces without a parent")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Root namespaces retrieved successfully")
        })
        public ResponseEntity<List<NamespaceDto>> getRoots(
                        @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                Long effectiveOrgId = resolveOrganizationId(organizationId, currentUser);
                if (effectiveOrgId == null) {
                        log.warn("GET /api/namespaces/roots - No organization context available");
                        return ResponseEntity.ok(Collections.emptyList());
                }

                log.debug("GET /api/namespaces/roots - Org: {}", effectiveOrgId);

                List<NamespaceDto> roots = namespaceService.getRoots(effectiveOrgId)
                                .stream()
                                .map(NamespaceMapper::toDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(roots);
        }

        /**
         * GET /api/namespaces/{id}/children - Get child namespaces.
         */
        @GetMapping("/{id}/children")
        @PreAuthorize("@accessControlService.canView(#id, authentication)")
        @Operation(summary = "Get child namespaces", description = "Get all direct child namespaces of a parent")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Child namespaces retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Parent namespace not found")
        })
        public ResponseEntity<List<NamespaceDto>> getChildren(
                        @Parameter(description = "Parent namespace ID") @PathVariable Long id,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/namespaces/{}/children - Org: {}", id, organizationId);

                List<NamespaceDto> children = namespaceService.getChildren(id, organizationId)
                                .stream()
                                .map(NamespaceMapper::toDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(children);
        }

        // Alias for backward compatibility
        @GetMapping("/{id}/namespaces")
        @PreAuthorize("@accessControlService.canView(#id, authentication)")
        public ResponseEntity<List<NamespaceDto>> getChildNamespaces(
                        @PathVariable Long id,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {
                return getChildren(id, organizationId, currentUser);
        }

        // ==================== CRUD OPERATIONS ====================

        /**
         * GET /api/namespaces/{id} - Get namespace by ID.
         */
        @GetMapping("/{id}")
        @PreAuthorize("@accessControlService.canView(#id, authentication)")
        @Operation(summary = "Get namespace", description = "Get namespace details by ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Namespace found"),
                        @ApiResponse(responseCode = "404", description = "Namespace not found")
        })
        public ResponseEntity<NamespaceDto> getById(
                        @Parameter(description = "Namespace ID") @PathVariable Long id,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/namespaces/{} - Org: {}", id, organizationId);

                Namespace ns = namespaceService.getById(id, organizationId);
                return ResponseEntity.ok(NamespaceMapper.toDto(ns));
        }

        /**
         * POST /api/namespaces - Create new namespace.
         */
        @PostMapping
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER')")
        @Operation(summary = "Create namespace", description = "Create a new namespace/folder")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Namespace created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid data"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<NamespaceDto> create(
                        @Valid @RequestBody NamespaceDto dto,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.info("POST /api/namespaces - Creating: '{}' in Org: {}", dto.getName(), organizationId);

                dto.setId(null);
                Namespace ns = NamespaceMapper.toEntity(dto);
                Namespace saved = namespaceService.create(ns, currentUser.getId(), dto.getParentId(), organizationId);

                return ResponseEntity.status(HttpStatus.CREATED).body(NamespaceMapper.toDto(saved));
        }

        /**
         * PUT /api/namespaces/{id} - Update namespace.
         */
        @PutMapping("/{id}")
        @PreAuthorize("@accessControlService.canEdit(#id, authentication)")
        @Operation(summary = "Update namespace", description = "Update namespace name, description, or parent")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Namespace updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Namespace not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<NamespaceDto> update(
                        @Parameter(description = "Namespace ID") @PathVariable Long id,
                        @Valid @RequestBody NamespaceDto dto,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.info("PUT /api/namespaces/{} - Updating", id);

                Namespace ns = NamespaceMapper.toEntity(dto);
                Namespace updated = namespaceService.update(id, ns, dto.getParentId(), organizationId);

                return ResponseEntity.ok(NamespaceMapper.toDto(updated));
        }

        /**
         * DELETE /api/namespaces/{id} - Delete namespace.
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("@accessControlService.canDelete(#id, authentication)")
        @Operation(summary = "Delete namespace", description = "Delete namespace and optionally its contents")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Namespace deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Namespace not found"),
                        @ApiResponse(responseCode = "409", description = "Namespace not empty")
        })
        public ResponseEntity<Void> delete(
                        @Parameter(description = "Namespace ID") @PathVariable Long id,
                        @Parameter(description = "Force delete including contents") @RequestParam(defaultValue = "false") boolean force,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.info("DELETE /api/namespaces/{} - Force: {}", id, force);

                namespaceService.delete(id, organizationId);
                return ResponseEntity.noContent().build();
        }

        // ==================== SEARCH & FILTER ====================

        /**
         * GET /api/namespaces/search - Search namespaces.
         */
        @GetMapping("/search")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER', 'READER')")
        @Operation(summary = "Search namespaces", description = "Search namespaces by name, parent, or creator")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Search results returned")
        })
        public ResponseEntity<List<NamespaceDto>> search(
                        @Parameter(description = "Filter by name (partial match)") @RequestParam(required = false) String name,
                        @Parameter(description = "Filter by parent namespace ID") @RequestParam(required = false) Long parentId,
                        @Parameter(description = "Filter by creator user ID") @RequestParam(required = false) Long createdById,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/namespaces/search - name={}, parentId={}, createdById={}",
                                name, parentId, createdById);

                List<NamespaceDto> results = namespaceService
                                .advancedSearch(name, parentId, createdById, organizationId)
                                .stream()
                                .map(NamespaceMapper::toDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(results);
        }

        /**
         * GET /api/namespaces/by-creator/{userId} - Get namespaces by creator.
         */
        @GetMapping("/by-creator/{userId}")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER') or #userId == authentication.principal.id")
        @Operation(summary = "Get namespaces by creator", description = "Get all namespaces created by a user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Namespaces retrieved successfully")
        })
        public ResponseEntity<List<NamespaceDto>> getByCreator(
                        @Parameter(description = "Creator user ID") @PathVariable Long userId,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/namespaces/by-creator/{}", userId);

                List<NamespaceDto> namespaces = namespaceService.findAllByCreatedById(userId, organizationId)
                                .stream()
                                .map(NamespaceMapper::toDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(namespaces);
        }

        /**
         * GET /api/namespaces/my - Get current user's namespaces.
         */
        @GetMapping("/my")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get my namespaces", description = "Get namespaces created by the current user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Namespaces retrieved successfully")
        })
        public ResponseEntity<List<NamespaceDto>> getMyNamespaces(
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                return getByCreator(currentUser.getId(), organizationId, currentUser);
        }

        // ==================== DOCUMENT OPERATIONS ====================

        /**
         * GET /api/namespaces/{id}/documents - Get documents in namespace.
         */
        @GetMapping("/{id}/documents")
        @PreAuthorize("@accessControlService.canView(#id, authentication)")
        @Operation(summary = "Get namespace documents", description = "Get all documents in a namespace")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Namespace not found")
        })
        public ResponseEntity<List<DocumentDto>> getDocuments(
                        @Parameter(description = "Namespace ID") @PathVariable Long id,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/namespaces/{}/documents", id);

                List<DocumentDto> documents = documentService
                                .getDocumentsByNamespace(id, organizationId, Pageable.unpaged(), currentUser.getId())
                                .getContent()
                                .stream()
                                .map(DocumentMapper::toDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(documents);
        }

        // ==================== TREE OPERATIONS ====================

        /**
         * GET /api/namespaces/{id}/path - Get namespace path (breadcrumb).
         */
        @GetMapping("/{id}/path")
        @PreAuthorize("@accessControlService.canView(#id, authentication)")
        @Operation(summary = "Get namespace path", description = "Get the full path from root to this namespace")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Path retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Namespace not found")
        })
        public ResponseEntity<List<NamespaceDto>> getPath(
                        @Parameter(description = "Namespace ID") @PathVariable Long id,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/namespaces/{}/path", id);

                List<NamespaceDto> path = namespaceService.getPath(id, organizationId)
                                .stream()
                                .map(NamespaceMapper::toDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(path);
        }

        /**
         * POST /api/namespaces/{id}/move - Move namespace to new parent.
         */
        @PostMapping("/{id}/move")
        @PreAuthorize("@accessControlService.canEdit(#id, authentication)")
        @Operation(summary = "Move namespace", description = "Move a namespace to a new parent")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Namespace moved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid move (circular reference)"),
                        @ApiResponse(responseCode = "404", description = "Namespace not found")
        })
        public ResponseEntity<NamespaceDto> move(
                        @Parameter(description = "Namespace ID to move") @PathVariable Long id,
                        @Parameter(description = "New parent ID (null for root)") @RequestParam(required = false) Long newParentId,
                        @RequestHeader("X-Organization-ID") Long organizationId,
                        @AuthenticationPrincipal User currentUser) {

                log.info("POST /api/namespaces/{}/move - New parent: {}", id, newParentId);

                Namespace moved = namespaceService.move(id, newParentId, organizationId);
                return ResponseEntity.ok(NamespaceMapper.toDto(moved));
        }

        // ==================== HELPER METHODS ====================

        /**
         * Resolves the organization ID from header or user context.
         */
        private Long resolveOrganizationId(Long headerOrgId, User currentUser) {
                if (headerOrgId != null) {
                        return headerOrgId;
                }
                if (currentUser.getOrganization() != null) {
                        return currentUser.getOrganization().getId();
                }
                if (currentUser.getDepartment() != null && currentUser.getDepartment().getOrganization() != null) {
                        return currentUser.getDepartment().getOrganization().getId();
                }
                return null;
        }
}
