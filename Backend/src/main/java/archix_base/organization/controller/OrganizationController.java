package archix_base.organization.controller;

import archix_base.identity.entity.User;
import archix_base.organization.dto.OrganizationDto;
import archix_base.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for organization management.
 * Organizations are the top-level tenants in the system.
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organizations", description = "Organization management API")
public class OrganizationController {

        private final OrganizationService organizationService;

        // ==================== LIST OPERATIONS ====================

        /**
         * GET /api/organizations - List all accessible organizations.
         */
        @GetMapping
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "List organizations", description = "Get all organizations accessible to the current user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<List<OrganizationDto>> getAllOrganizations(
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/organizations - User: {}", currentUser.getEmail());
                return ResponseEntity.ok(organizationService.getAllOrganizations(currentUser));
        }

        // ==================== CRUD OPERATIONS ====================

        /**
         * GET /api/organizations/{id} - Get organization by ID.
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('SUPER_ADMIN') or @organizationSecurityService.isMember(#id, authentication)")
        @Operation(summary = "Get organization", description = "Get organization details by ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Organization found"),
                        @ApiResponse(responseCode = "404", description = "Organization not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<OrganizationDto> getOrganizationById(
                        @Parameter(description = "Organization ID") @PathVariable Long id,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/organizations/{}", id);
                return ResponseEntity.ok(organizationService.getOrganizationById(id));
        }

        /**
         * POST /api/organizations - Create new organization.
         */
        @PostMapping
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @Operation(summary = "Create organization", description = "Create a new organization (SuperAdmin only)")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Organization created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin required")
        })
        public ResponseEntity<OrganizationDto> createOrganization(
                        @Valid @RequestBody OrganizationDto organizationDto,
                        @AuthenticationPrincipal User currentUser) {

                log.info("POST /api/organizations - Creating: '{}' by {}",
                                organizationDto.getName(), currentUser.getEmail());

                OrganizationDto created = organizationService.createOrganization(organizationDto);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        /**
         * PUT /api/organizations/{id} - Update organization.
         */
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('ADMIN') and @organizationSecurityService.isAdmin(#id, authentication))")
        @Operation(summary = "Update organization", description = "Update organization details")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Organization updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Organization not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<OrganizationDto> updateOrganization(
                        @Parameter(description = "Organization ID") @PathVariable Long id,
                        @Valid @RequestBody OrganizationDto organizationDto,
                        @AuthenticationPrincipal User currentUser) {

                log.info("PUT /api/organizations/{} - Updating by {}", id, currentUser.getEmail());

                organizationDto.setId(id);
                return ResponseEntity.ok(organizationService.updateOrganization(organizationDto));
        }

        /**
         * PUT /api/organizations - Update organization (legacy endpoint).
         */
        @PutMapping
        @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('ADMIN') and @organizationSecurityService.isAdmin(#organizationDto.id, authentication))")
        @Operation(summary = "Update organization (legacy)", description = "Update organization using body ID")
        @Deprecated
        public ResponseEntity<OrganizationDto> updateOrganizationLegacy(
                        @Valid @RequestBody OrganizationDto organizationDto,
                        @AuthenticationPrincipal User currentUser) {

                log.info("PUT /api/organizations (legacy) - Updating ID: {} by {}",
                                organizationDto.getId(), currentUser.getEmail());

                return ResponseEntity.ok(organizationService.updateOrganization(organizationDto));
        }

        /**
         * DELETE /api/organizations/{id} - Delete organization.
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @Operation(summary = "Delete organization", description = "Delete an organization (SuperAdmin only)")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Organization deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Organization not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin required"),
                        @ApiResponse(responseCode = "409", description = "Cannot delete - organization has active users/documents")
        })
        public ResponseEntity<Void> deleteOrganization(
                        @Parameter(description = "Organization ID") @PathVariable Long id,
                        @AuthenticationPrincipal User currentUser) {

                log.warn("DELETE /api/organizations/{} - Deleting by {}", id, currentUser.getEmail());

                organizationService.deleteOrganizationById(id);
                return ResponseEntity.noContent().build();
        }

        // ==================== ORGANIZATION STATS ====================

        /**
         * GET /api/organizations/{id}/stats - Get organization statistics.
         */
        @GetMapping("/{id}/stats")
        @PreAuthorize("hasRole('SUPER_ADMIN') or (hasAnyRole('ADMIN', 'MANAGER') and @organizationSecurityService.isMember(#id, authentication))")
        @Operation(summary = "Get organization stats", description = "Get statistics for an organization")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Organization not found")
        })
        public ResponseEntity<OrganizationDto> getOrganizationStats(
                        @Parameter(description = "Organization ID") @PathVariable Long id,
                        @AuthenticationPrincipal User currentUser) {

                log.debug("GET /api/organizations/{}/stats", id);

                // Returns organization with stats populated
                return ResponseEntity.ok(organizationService.getOrganizationWithStats(id));
        }
}
