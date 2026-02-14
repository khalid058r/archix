package archix_base.organization.dto;

import archix_base.organization.entity.PlanType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for organization data with SaaS plan details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationResponse {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String phone;
    private String email;
    private String logoUrl;
    
    // Plan & Quotas
    private PlanType plan;
    private String planDisplayName;
    private Long storageUsedBytes;
    private Long storageQuotaBytes;
    private Double storageUsagePercentage;
    private Integer maxUsers;
    private Integer currentUserCount;
    private Integer maxDocuments;
    private Integer currentDocumentCount;
    
    // Settings (parsed from JSON)
    private Object settings;
    
    // Status
    private Boolean isActive;
    private LocalDateTime suspendedAt;
    private String suspendedReason;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Creator info
    private Long createdById;
    private String createdByName;
    
    // Computed helpers
    public String getStorageUsedFormatted() {
        return formatBytes(storageUsedBytes);
    }
    
    public String getStorageQuotaFormatted() {
        if (storageQuotaBytes == null || storageQuotaBytes == Long.MAX_VALUE) {
            return "Unlimited";
        }
        return formatBytes(storageQuotaBytes);
    }
    
    public boolean isNearStorageLimit() {
        return storageUsagePercentage != null && storageUsagePercentage >= 80.0;
    }
    
    public boolean canAddMoreUsers() {
        if (maxUsers == null || maxUsers == Integer.MAX_VALUE) return true;
        return currentUserCount == null || currentUserCount < maxUsers;
    }
    
    private static String formatBytes(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
