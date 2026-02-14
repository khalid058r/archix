package archix_base.audit.service;

import archix_base.audit.controller.AuditController.AuditStatsDto;
import archix_base.audit.entity.AuditLog;
import archix_base.audit.repo.AuditLogRepo;
import archix_base.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepo auditLogRepo;

    /**
     * Log an audit event.
     */
    public void log(String action, String entityName, String entityId, Long userId, String username, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setDetails(details);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepo.save(auditLog);
        
        log.debug("Audit logged: {} {} {} by {}", action, entityName, entityId, username);
    }

    /**
     * Get all audit logs.
     */
    public List<AuditLog> getAllLogs() {
        return auditLogRepo.findAllByOrderByTimestampDesc();
    }

    /**
     * Get audit log by ID.
     */
    public AuditLog getLogById(Long id) {
        return auditLogRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found: " + id));
    }

    /**
     * Get paginated audit logs with filters.
     */
    public Page<AuditLog> getLogsPaginated(String action, String entityType, Long userId,
                                            LocalDateTime startDate, LocalDateTime endDate,
                                            Long organizationId, Pageable pageable) {
        List<AuditLog> allLogs = auditLogRepo.findAllByOrderByTimestampDesc();
        
        // Apply filters
        List<AuditLog> filtered = allLogs.stream()
                .filter(l -> action == null || action.equalsIgnoreCase(l.getAction()))
                .filter(l -> entityType == null || entityType.equalsIgnoreCase(l.getEntityName()))
                .filter(l -> userId == null || userId.equals(l.getUserId()))
                .filter(l -> startDate == null || !l.getTimestamp().isBefore(startDate))
                .filter(l -> endDate == null || !l.getTimestamp().isAfter(endDate))
                .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    /**
     * Get audit logs by user.
     */
    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepo.findAllByOrderByTimestampDesc().stream()
                .filter(l -> userId.equals(l.getUserId()))
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs by entity.
     */
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepo.findAllByOrderByTimestampDesc().stream()
                .filter(l -> entityType.equalsIgnoreCase(l.getEntityName()))
                .filter(l -> entityId.toString().equals(l.getEntityId()))
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs by action.
     */
    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepo.findAllByOrderByTimestampDesc().stream()
                .filter(l -> action.equalsIgnoreCase(l.getAction()))
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs by date range.
     */
    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepo.findAllByOrderByTimestampDesc().stream()
                .filter(l -> !l.getTimestamp().isBefore(startDate))
                .filter(l -> !l.getTimestamp().isAfter(endDate))
                .collect(Collectors.toList());
    }

    /**
     * Get audit statistics.
     */
    public AuditStatsDto getStats(Long organizationId) {
        List<AuditLog> allLogs = auditLogRepo.findAllByOrderByTimestampDesc();
        
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        
        long totalLogs = allLogs.size();
        long todayLogs = allLogs.stream()
                .filter(l -> !l.getTimestamp().isBefore(todayStart) && !l.getTimestamp().isAfter(todayEnd))
                .count();
        long createActions = allLogs.stream().filter(l -> "CREATE".equalsIgnoreCase(l.getAction())).count();
        long updateActions = allLogs.stream().filter(l -> "UPDATE".equalsIgnoreCase(l.getAction())).count();
        long deleteActions = allLogs.stream().filter(l -> "DELETE".equalsIgnoreCase(l.getAction())).count();
        long viewActions = allLogs.stream().filter(l -> "VIEW".equalsIgnoreCase(l.getAction())).count();
        
        return new AuditStatsDto(totalLogs, todayLogs, createActions, updateActions, deleteActions, viewActions);
    }
}
