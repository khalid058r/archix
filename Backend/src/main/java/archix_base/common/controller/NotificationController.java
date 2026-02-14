package archix_base.common.controller;

import archix_base.common.dto.NotificationDto;
import archix_base.common.response.ApiResponse;
import archix_base.common.service.NotificationService;
import archix_base.identity.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications API")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications - Get current user's notifications.
     */
    @GetMapping
    @Operation(summary = "Get notifications", description = "Get paginated notifications for the current user")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<NotificationDto> page = notificationService.getNotifications(
                currentUser.getId(), unreadOnly, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * GET /api/notifications/unread-count - Get unread count.
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Unread count", description = "Get unread notification count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(
            @AuthenticationPrincipal User currentUser) {

        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /**
     * PUT /api/notifications/{id}/read - Mark a notification as read.
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark as read", description = "Mark a specific notification as read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        NotificationDto dto = notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * PUT /api/notifications/read-all - Mark all as read.
     */
    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead(
            @AuthenticationPrincipal User currentUser) {

        int count = notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("marked", count)));
    }

    /**
     * DELETE /api/notifications/read - Delete all read notifications.
     */
    @DeleteMapping("/read")
    @Operation(summary = "Delete read notifications", description = "Delete all read notifications")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> deleteRead(
            @AuthenticationPrincipal User currentUser) {

        int count = notificationService.deleteRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("deleted", count)));
    }
}
