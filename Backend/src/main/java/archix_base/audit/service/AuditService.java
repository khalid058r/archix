package archix_base.audit.service;

import archix_base.audit.entity.AuditLog;
import archix_base.audit.repo.AuditLogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepo auditLogRepo;

    public void log(String action, String entityName, String entityId, Long userId, String username, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setUserId(userId);
        log.setUsername(username);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepo.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepo.findAllByOrderByTimestampDesc();
    }
}
