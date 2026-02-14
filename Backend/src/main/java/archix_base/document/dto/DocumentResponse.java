package archix_base.document.dto;

import archix_base.document.entity.DocumentStatus;
import archix_base.document.entity.Document.ProcessingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for document data.
 * Includes computed fields and presigned URLs for frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponse {

    private Long id;
    private String name;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String mimeType;
    private String description;
    private String tags;
    
    // Status & workflow
    private DocumentStatus status;
    private ProcessingStatus processingStatus;
    
    // Version info
    private Integer version;
    private Boolean isLatestVersion;
    private Long previousVersionId;
    private Long latestVersionId;
    
    // Storage info (no paths exposed for security)
    private String checksum;
    private Boolean hasThumbnail;
    
    // Access tracking
    private Integer downloadCount;
    private LocalDateTime lastAccessedAt;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relations
    private Long createdById;
    private String createdByName;
    private String createdByEmail;
    private Long parentId;
    private String parentName;
    private Long organizationId;
    
    // URLs (presigned, short-lived)
    private String downloadUrl;
    private String thumbnailUrl;
    
    // Computed helpers
    public String getFileSizeFormatted() {
        if (fileSize == null) return "0 B";
        
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = fileSize;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
    
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }
    
    public boolean isOfficeDocument() {
        if (mimeType == null) return false;
        return mimeType.contains("officedocument") || 
               mimeType.contains("msword") ||
               mimeType.contains("ms-excel") ||
               mimeType.contains("ms-powerpoint");
    }
}
