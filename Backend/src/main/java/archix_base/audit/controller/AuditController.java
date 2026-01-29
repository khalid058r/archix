package archix_base.audit.controller;

import archix_base.audit.entity.AuditLog;
import archix_base.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }
}
