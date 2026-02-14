package archix_base.organization.controller;

import archix_base.identity.entity.User;
import archix_base.organization.dto.ChangeOrganizationDto;
import archix_base.organization.dto.CreateDepartmentDto;
import archix_base.organization.dto.DepartmentDto;
import archix_base.organization.dto.DepartmentStatsDto;
import archix_base.organization.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * REST Controller for Department management.
 * Provides CRUD operations, hierarchy management, and statistics for
 * departments within organizations.
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Department", description = "Department management APIs - organizational unit operations")
public class DepartmentController {

    private final DepartmentService departmentService;

    // =====================================================
    // READ OPERATIONS
    // =====================================================

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all departments in organization", description = "Retrieves all departments for the authenticated user's organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<List<DepartmentDto>> getAllDepartments(
            @Parameter(description = "Organization ID from header") @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {
        Long effectiveOrgId = resolveOrganizationId(organizationId, currentUser);
        if (effectiveOrgId == null) {
            log.warn("GET /api/departments - No organization context available");
            return ResponseEntity.ok(Collections.emptyList());
        }
        log.info("GET /api/departments - Organization: {}", effectiveOrgId);
        return ResponseEntity.ok(departmentService.getAllDepartments(effectiveOrgId));
    }

    @GetMapping("/paginated")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get departments with pagination", description = "Retrieves departments with pagination and sorting support")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated departments retrieved"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Page<DepartmentDto>> getDepartmentsPaginated(
            @RequestHeader("X-Organization-ID") Long organizationId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("GET /api/departments/paginated - Organization: {}, Page: {}", organizationId,
                pageable.getPageNumber());
        return ResponseEntity.ok(departmentService.getDepartmentsPaginated(organizationId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get department by ID", description = "Retrieves a specific department by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department found"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentDto> getDepartmentById(
            @Parameter(description = "Department ID", required = true) @PathVariable Long id) {
        log.info("GET /api/departments/{}", id);
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("@organizationSecurityService.isMember(#organizationId, authentication)")
    @Operation(summary = "Get departments by organization", description = "Retrieves all departments for a specific organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departments retrieved"),
            @ApiResponse(responseCode = "403", description = "Not a member of this organization"),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<List<DepartmentDto>> getDepartmentsByOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long organizationId) {
        log.info("GET /api/departments/organization/{}", organizationId);
        return ResponseEntity.ok(departmentService.getDepartmentsByOrganization(organizationId));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get department statistics", description = "Retrieves statistics for a department including user count, document count, etc.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentStatsDto> getDepartmentStats(
            @Parameter(description = "Department ID", required = true) @PathVariable Long id) {
        log.info("GET /api/departments/{}/stats", id);
        return ResponseEntity.ok(departmentService.getDepartmentStats(id));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search departments", description = "Search departments by name within the organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned"),
            @ApiResponse(responseCode = "400", description = "Invalid search query")
    })
    public ResponseEntity<List<DepartmentDto>> searchDepartments(
            @RequestHeader("X-Organization-ID") Long organizationId,
            @Parameter(description = "Search query (name contains)", required = true) @RequestParam String q) {
        log.info("GET /api/departments/search - Organization: {}, Query: {}", organizationId, q);
        return ResponseEntity.ok(departmentService.searchDepartments(organizationId, q));
    }

    @GetMapping("/hierarchy")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get department hierarchy", description = "Returns departments with their parent-child relationships")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hierarchy retrieved")
    })
    public ResponseEntity<List<DepartmentDto>> getDepartmentHierarchy(
            @RequestHeader("X-Organization-ID") Long organizationId) {
        log.info("GET /api/departments/hierarchy - Organization: {}", organizationId);
        return ResponseEntity.ok(departmentService.getDepartmentHierarchy(organizationId));
    }

    // =====================================================
    // CREATE OPERATIONS
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new department", description = "Creates a new department within the organization. Requires ADMIN or MANAGER role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Department created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Not authorized to create departments"),
            @ApiResponse(responseCode = "409", description = "Department with this name already exists")
    })
    public ResponseEntity<DepartmentDto> createDepartment(
            @Valid @RequestBody CreateDepartmentDto createDepartmentDto) {
        log.info("POST /api/departments - Creating department: {}", createDepartmentDto.getName());
        DepartmentDto created = departmentService.createDepartment(createDepartmentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =====================================================
    // UPDATE OPERATIONS
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update a department", description = "Updates an existing department. Requires ADMIN or MANAGER role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update departments"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentDto> updateDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable Long id,
            @Valid @RequestBody DepartmentDto departmentDto) {
        log.info("PUT /api/departments/{} - Updating department", id);
        departmentDto.setId(id);
        return ResponseEntity.ok(departmentService.updateDepartment(departmentDto));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update a department (legacy)", description = "Updates an existing department using ID from request body")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @Deprecated(since = "1.0", forRemoval = true)
    public ResponseEntity<DepartmentDto> updateDepartmentLegacy(
            @Valid @RequestBody DepartmentDto departmentDto) {
        log.info("PUT /api/departments - Updating department (legacy): {}", departmentDto.getId());
        return ResponseEntity.ok(departmentService.updateDepartment(departmentDto));
    }

    @PutMapping("/change-org")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move department to another organization", description = "Transfers a department to a different organization. ADMIN only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department moved successfully"),
            @ApiResponse(responseCode = "403", description = "Only ADMINs can move departments between organizations"),
            @ApiResponse(responseCode = "404", description = "Department or organization not found")
    })
    public ResponseEntity<DepartmentDto> changeOrganization(
            @Valid @RequestBody ChangeOrganizationDto changeOrganizationDto) {
        log.info("PUT /api/departments/change-org - Moving department {} to organization {}",
                changeOrganizationDto.getId(), changeOrganizationDto.getOrganizationId());
        return ResponseEntity.ok(departmentService.changeOrganization(changeOrganizationDto));
    }

    @PutMapping("/{id}/parent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Change department parent", description = "Move a department under a different parent department")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parent changed successfully"),
            @ApiResponse(responseCode = "400", description = "Circular reference detected"),
            @ApiResponse(responseCode = "404", description = "Department or parent not found")
    })
    public ResponseEntity<DepartmentDto> changeParent(
            @Parameter(description = "Department ID", required = true) @PathVariable Long id,
            @Parameter(description = "New parent department ID (null for root)") @RequestParam(required = false) Long parentId,
            @RequestHeader("X-Organization-ID") Long organizationId) {
        log.info("PUT /api/departments/{}/parent - Changing parent to: {}", id, parentId);
        return ResponseEntity.ok(departmentService.changeParent(id, parentId, organizationId));
    }

    // =====================================================
    // DELETE OPERATIONS
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a department", description = "Deletes a department. ADMIN only. Will fail if department has users.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete - department has users"),
            @ApiResponse(responseCode = "403", description = "Only ADMINs can delete departments"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable Long id) {
        log.info("DELETE /api/departments/{}", id);
        departmentService.deleteDepartmentById(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // MY DEPARTMENT OPERATIONS
    // =====================================================

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's department", description = "Retrieves the department of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department found"),
            @ApiResponse(responseCode = "404", description = "User has no department")
    })
    public ResponseEntity<DepartmentDto> getMyDepartment(@AuthenticationPrincipal User user) {
        log.info("GET /api/departments/my - User: {}", user.getEmail());
        return ResponseEntity.ok(departmentService.getUserDepartment(user.getId()));
    }

    @GetMapping("/my/colleagues")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get department colleagues", description = "Retrieves all users in the same department as the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Colleagues retrieved"),
            @ApiResponse(responseCode = "404", description = "User has no department")
    })
    public ResponseEntity<List<DepartmentDto>> getMyDepartmentColleagues(@AuthenticationPrincipal User user) {
        log.info("GET /api/departments/my/colleagues - User: {}", user.getEmail());
        // Returns the department with user info for colleagues feature
        DepartmentDto dept = departmentService.getUserDepartment(user.getId());
        return ResponseEntity.ok(List.of(dept));
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
