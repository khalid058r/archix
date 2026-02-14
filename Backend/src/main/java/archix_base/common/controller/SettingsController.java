package archix_base.common.controller;

import archix_base.common.entity.SystemSetting;
import archix_base.common.response.ApiResponse;
import archix_base.common.service.SystemSettingService;
import archix_base.identity.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "System settings management API")
public class SettingsController {

    private final SystemSettingService settingService;

    /**
     * GET /api/settings - Get all settings for the organization.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get all settings", description = "Get all settings merged from defaults, global, and organization level")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAll(
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId) {

        Map<String, Object> settings = settingService.getAllSettings(organizationId);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * GET /api/settings/{key} - Get a specific setting value.
     */
    @GetMapping("/{key}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get setting", description = "Get a specific setting value")
    public ResponseEntity<ApiResponse<Map<String, String>>> getByKey(
            @PathVariable String key,
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId) {

        String value = settingService.getValue(key, organizationId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("key", key, "value", value != null ? value : "")));
    }

    /**
     * PUT /api/settings - Create or update a setting.
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update setting", description = "Create or update a system setting")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSetting(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {

        String key = payload.get("key");
        String value = payload.get("value");
        String description = payload.get("description");
        String type = payload.get("type");

        SystemSetting saved = settingService.setSetting(key, value, description, type,
                organizationId, currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("id", saved.getId(), "key", saved.getKey(), "value", saved.getValue()),
                "Setting updated successfully"));
    }

    /**
     * PUT /api/settings/bulk - Update multiple settings at once.
     */
    @PutMapping("/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Bulk update settings", description = "Update multiple settings at once")
    public ResponseEntity<ApiResponse<String>> bulkUpdate(
            @RequestBody Map<String, String> settings,
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {

        settings.forEach(
                (key, value) -> settingService.setSetting(key, value, null, null, organizationId, currentUser.getId()));

        return ResponseEntity.ok(ApiResponse.success("Settings updated", settings.size() + " settings updated"));
    }

    /**
     * DELETE /api/settings/{id} - Delete a setting.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete setting", description = "Delete a specific setting")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        settingService.deleteSetting(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/settings/global - Get only global settings (SUPER_ADMIN).
     */
    @GetMapping("/global")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get global settings", description = "Get global settings only")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGlobal() {
        Map<String, Object> settings = settingService.getAllSettings(null);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
}
