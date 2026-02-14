package archix_base.common.service;

import archix_base.common.dto.NotificationDto;
import archix_base.common.entity.Notification;
import archix_base.common.exception.EntityNotFoundException;
import archix_base.common.repo.NotificationRepo;
import archix_base.identity.entity.User;
import archix_base.identity.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;

    /**
     * Create and send a notification to a user.
     */
    public NotificationDto notify(Long userId, String title, String message,
            String type, String entityType, String entityId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .entityType(entityType)
                .entityId(entityId)
                .build();

        return toDto(notificationRepo.save(notification));
    }

    /**
     * Get paginated notifications for a user.
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(Long userId, boolean unreadOnly, Pageable pageable) {
        Page<Notification> page = unreadOnly
                ? notificationRepo.findAllByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable)
                : notificationRepo.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

        return page.map(this::toDto);
    }

    /**
     * Get unread count for a user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepo.countByUserIdAndReadFalse(userId);
    }

    /**
     * Mark a single notification as read.
     */
    public NotificationDto markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityException("Cannot mark another user's notification");
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return toDto(notificationRepo.save(notification));
    }

    /**
     * Mark all notifications as read.
     */
    public int markAllAsRead(Long userId) {
        return notificationRepo.markAllReadByUserId(userId);
    }

    /**
     * Delete read notifications for a user.
     */
    public int deleteRead(Long userId) {
        return notificationRepo.deleteAllReadByUserId(userId);
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .entityType(n.getEntityType())
                .entityId(n.getEntityId())
                .read(n.isRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
