package archix_base.common.controller;

import archix_base.audit.repo.AuditLogRepo;
import archix_base.common.response.ApiResponse;
import archix_base.document.repo.DocumentRepo;
import archix_base.identity.repo.UserRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reports & statistics API")
public class ReportController {

    private final DocumentRepo documentRepo;
    private final AuditLogRepo auditLogRepo;
    private final UserRepo userRepo;

    /**
     * GET /api/reports/dashboard - Main dashboard stats.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Dashboard statistics", description = "Get summary stats for the dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard(
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId) {

        Map<String, Object> stats = new LinkedHashMap<>();

        // Document count
        long docCount = organizationId != null
                ? documentRepo.countByOrganizationIdAndIsDeletedFalse(organizationId)
                : documentRepo.count();
        stats.put("totalDocuments", docCount);

        // Documents by status
        List<Object[]> statusCounts = organizationId != null
                ? documentRepo.countByStatusAndOrganizationId(organizationId)
                : documentRepo.countByStatus();
        Map<String, Long> byStatus = statusCounts.stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> (Long) r[1],
                        (a, b) -> a,
                        LinkedHashMap::new));
        stats.put("documentsByStatus", byStatus);

        // Storage usage
        Long totalSize = organizationId != null
                ? documentRepo.sumFileSizeByOrganizationId(organizationId)
                : null;
        stats.put("totalStorageBytes", totalSize != null ? totalSize : 0);
        stats.put("totalStorageMB", totalSize != null ? totalSize / (1024.0 * 1024.0) : 0);

        // User count
        long userCount = organizationId != null
                ? userRepo.countByOrganizationId(organizationId)
                : userRepo.count();
        stats.put("totalUsers", userCount);

        // Recent activity (last 24h)
        stats.put("actionsLast24h", auditLogRepo.countByTimestampAfter(
                LocalDateTime.now().minusHours(24)));

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * GET /api/reports/documents/by-type - Documents grouped by MIME type.
     */
    @GetMapping("/documents/by-type")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Documents by type", description = "Get document counts grouped by MIME type")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> documentsByType(
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId) {

        if (organizationId == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        List<Map<String, Object>> result = documentRepo.countByMimeTypeAndOrganizationId(organizationId)
                .stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("mimeType", r[0]);
                    m.put("count", r[1]);
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/reports/activity - Activity timeline for charts.
     */
    @GetMapping("/activity")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Activity timeline", description = "Get daily activity counts for a given number of days")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> activityTimeline(
            @RequestParam(defaultValue = "30") int days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        List<Map<String, Object>> result = auditLogRepo.countByDaySince(since)
                .stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("date", r[0].toString());
                    m.put("count", r[1]);
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/reports/activity/by-action - Actions breakdown.
     */
    @GetMapping("/activity/by-action")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Activity by action", description = "Get activity counts by action type")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> activityByAction(
            @RequestParam(defaultValue = "30") int days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        List<Map<String, Object>> result = auditLogRepo.countByActionSince(since)
                .stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("action", r[0]);
                    m.put("count", r[1]);
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/reports/activity/top-users - Most active users.
     */
    @GetMapping("/activity/top-users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Top active users", description = "Get most active users by action count")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> topUsers(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        List<Map<String, Object>> result = auditLogRepo.countByUserSince(since)
                .stream()
                .limit(limit)
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId", r[0]);
                    m.put("actionCount", r[1]);
                    // Optionally resolve username
                    userRepo.findById((Long) r[0]).ifPresent(u -> {
                        m.put("username", u.getUsername());
                        m.put("fullName", u.getFirstName() + " " + u.getLastName());
                    });
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/reports/storage - Storage usage breakdown.
     */
    @GetMapping("/storage")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Storage usage", description = "Get storage usage breakdown by file type")
    public ResponseEntity<ApiResponse<Map<String, Object>>> storage(
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId) {

        Map<String, Object> result = new LinkedHashMap<>();

        if (organizationId != null) {
            Long totalBytes = documentRepo.sumFileSizeByOrganizationId(organizationId);
            result.put("totalBytes", totalBytes != null ? totalBytes : 0);
            result.put("totalMB", totalBytes != null ? Math.round(totalBytes / (1024.0 * 1024.0) * 100.0) / 100.0 : 0);
            result.put("totalGB",
                    totalBytes != null ? Math.round(totalBytes / (1024.0 * 1024.0 * 1024.0) * 1000.0) / 1000.0 : 0);
            result.put("documentCount", documentRepo.countByOrganizationIdAndIsDeletedFalse(organizationId));

            List<Map<String, Object>> byType = documentRepo.countByMimeTypeAndOrganizationId(organizationId)
                    .stream()
                    .map(r -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("mimeType", r[0]);
                        m.put("count", r[1]);
                        return m;
                    })
                    .collect(Collectors.toList());
            result.put("byType", byType);
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
