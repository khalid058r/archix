package archix_base.common.service;

import archix_base.audit.service.AuditService;
import archix_base.common.entity.SystemSetting;
import archix_base.common.exception.EntityNotFoundException;
import archix_base.common.repo.SystemSettingRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SystemSettingService {

    private final SystemSettingRepo settingRepo;
    private final AuditService auditService;

    // ============ Default settings ============
    private static final Map<String, String> DEFAULTS = Map.of(
            "max.file.size.mb", "50",
            "retention.days", "365",
            "auto.archive.enabled", "false",
            "allowed.mime.types",
            "application/pdf,image/png,image/jpeg,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "max.versions.per.document", "50",
            "require.approval.workflow", "true",
            "trash.auto.purge.days", "30");

    /**
     * Get a setting value (org-specific > global > default).
     */
    @Transactional(readOnly = true)
    public String getValue(String key, Long organizationId) {
        // 1. Organization-level setting
        if (organizationId != null) {
            var orgSetting = settingRepo.findByKeyAndOrganizationId(key, organizationId);
            if (orgSetting.isPresent())
                return orgSetting.get().getValue();
        }

        // 2. Global setting
        var globalSetting = settingRepo.findByKeyAndOrganizationIdIsNull(key);
        if (globalSetting.isPresent())
            return globalSetting.get().getValue();

        // 3. Default
        return DEFAULTS.getOrDefault(key, null);
    }

    /**
     * Get all settings for an organization (merged with globals and defaults).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllSettings(Long organizationId) {
        Map<String, Object> settings = new HashMap<>();

        // Start with defaults
        DEFAULTS.forEach((k, v) -> settings.put(k, Map.of(
                "key", k, "value", v, "source", "default")));

        // Override with global settings
        settingRepo.findAllByOrganizationIdIsNull().forEach(s -> settings.put(s.getKey(), Map.of(
                "key", s.getKey(), "value", s.getValue(),
                "description", s.getDescription() != null ? s.getDescription() : "",
                "type", s.getType() != null ? s.getType() : "STRING",
                "source", "global")));

        // Override with org-specific settings
        if (organizationId != null) {
            settingRepo.findAllByOrganizationId(organizationId).forEach(s -> settings.put(s.getKey(), Map.of(
                    "key", s.getKey(), "value", s.getValue(),
                    "description", s.getDescription() != null ? s.getDescription() : "",
                    "type", s.getType() != null ? s.getType() : "STRING",
                    "source", "organization")));
        }

        return settings;
    }

    /**
     * Set a setting (create or update).
     */
    public SystemSetting setSetting(String key, String value, String description,
            String type, Long organizationId, Long userId) {
        SystemSetting setting;

        if (organizationId != null) {
            setting = settingRepo.findByKeyAndOrganizationId(key, organizationId)
                    .orElse(SystemSetting.builder()
                            .key(key)
                            .organizationId(organizationId)
                            .build());
        } else {
            setting = settingRepo.findByKeyAndOrganizationIdIsNull(key)
                    .orElse(SystemSetting.builder()
                            .key(key)
                            .build());
        }

        setting.setValue(value);
        if (description != null)
            setting.setDescription(description);
        if (type != null)
            setting.setType(type);
        setting.setUpdatedById(userId);

        SystemSetting saved = settingRepo.save(setting);

        auditService.log("UPDATE_SETTING", "SystemSetting", key,
                userId, null, "Updated setting: " + key + " = " + value);

        return saved;
    }

    /**
     * Delete a setting (revert to default or global).
     */
    public void deleteSetting(Long id, Long userId) {
        SystemSetting setting = settingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Setting not found: " + id));

        settingRepo.delete(setting);

        auditService.log("DELETE_SETTING", "SystemSetting", setting.getKey(),
                userId, null, "Deleted setting: " + setting.getKey());
    }
}
