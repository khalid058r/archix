package archix_base.audit.controller;

import archix_base.audit.entity.AuditLog;
import archix_base.audit.service.AuditService;
import archix_base.common.response.PageResponse;
import archix_base.identity.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST API controller for audit log management.
 * Provides read-only access to system audit trails.
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "Audit trail management API")
public class AuditController {

    private final AuditService auditService;

    // ==================== LIST OPERATIONS ====================

    /**
     * GET /api/admin/audit-logs - Get all audit logs.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "List audit logs", description = "Get all audit logs with optional pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin required")
    })
    public ResponseEntity<List<AuditLog>> getLogs(
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {
        
        log.debug("GET /api/admin/audit-logs - Org: {}, User: {}", organizationId, currentUser.getEmail());
        
        List<AuditLog> logs = auditService.getAllLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/admin/audit-logs/paginated - Get paginated audit logs.
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "List audit logs (paginated)", description = "Get paginated audit logs with filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    })
    public ResponseEntity<PageResponse<AuditLog>> getLogsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Filter by action type") @RequestParam(required = false) String action,
            @Parameter(description = "Filter by entity type") @RequestParam(required = false) String entityType,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Start date filter") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date filter") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {
        
        log.debug("GET /api/admin/audit-logs/paginated - Filters: action={}, entityType={}, userId={}", 
                action, entityType, userId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> result = auditService.getLogsPaginated(
                action, entityType, userId, startDate, endDate, organizationId, pageable);
        
        return ResponseEntity.ok(PageResponse.of(result));
    }

    /**
     * GET /api/admin/audit-logs/{id} - Get audit log by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get audit log", description = "Get audit log details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit log found"),
            @ApiResponse(responseCode = "404", description = "Audit log not found")
    })
    public ResponseEntity<AuditLog> getLogById(
            @Parameter(description = "Audit log ID") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        log.debug("GET /api/admin/audit-logs/{}", id);
        
        AuditLog auditLog = auditService.getLogById(id);
        return ResponseEntity.ok(auditLog);
    }

    // ==================== SEARCH OPERATIONS ====================

    /**
     * GET /api/admin/audit-logs/by-user/{userId} - Get audit logs for a user.
     */
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get logs by user", description = "Get all audit logs for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    })
    public ResponseEntity<List<AuditLog>> getLogsByUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {
        
        log.debug("GET /api/admin/audit-logs/by-user/{}", userId);
        
        List<AuditLog> logs = auditService.getLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/admin/audit-logs/by-entity/{entityType}/{entityId} - Get logs for entity.
     */
    @GetMapping("/by-entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get logs by entity", description = "Get all audit logs for a specific entity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    })
    public ResponseEntity<List<AuditLog>> getLogsByEntity(
            @Parameter(description = "Entity type (e.g., Document, User, Namespace)") @PathVariable String entityType,
            @Parameter(description = "Entity ID") @PathVariable Long entityId,
            @AuthenticationPrincipal User currentUser) {
        
        log.debug("GET /api/admin/audit-logs/by-entity/{}/{}", entityType, entityId);
        
        List<AuditLog> logs = auditService.getLogsByEntity(entityType, entityId);
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/admin/audit-logs/by-action/{action} - Get logs by action type.
     */
    @GetMapping("/by-action/{action}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get logs by action", description = "Get all audit logs for a specific action type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    })
    public ResponseEntity<List<AuditLog>> getLogsByAction(
            @Parameter(description = "Action type (e.g., CREATE, UPDATE, DELETE, VIEW)") @PathVariable String action,
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {
        
        log.debug("GET /api/admin/audit-logs/by-action/{}", action);
        
        List<AuditLog> logs = auditService.getLogsByAction(action);
        return ResponseEntity.ok(logs);
    }

    // ==================== DATE RANGE OPERATIONS ====================

    /**
     * GET /api/admin/audit-logs/date-range - Get logs within date range.
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get logs by date range", description = "Get audit logs within a specific date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    })
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(
            @Parameter(description = "Start date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {
        
        log.debug("GET /api/admin/audit-logs/date-range - {} to {}", startDate, endDate);
        
        List<AuditLog> logs = auditService.getLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    // ==================== STATISTICS ====================

    /**
     * GET /api/admin/audit-logs/stats - Get audit log statistics.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get audit statistics", description = "Get statistics about audit logs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<AuditStatsDto> getStats(
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {
        
        log.debug("GET /api/admin/audit-logs/stats - Org: {}", organizationId);
        
        AuditStatsDto stats = auditService.getStats(organizationId);
        return ResponseEntity.ok(stats);
    }

    /**
     * DTO for audit statistics.
     */
    public record AuditStatsDto(
            long totalLogs,
            long todayLogs,
            long createActions,
            long updateActions,
            long deleteActions,
            long viewActions
    ) {}
}
