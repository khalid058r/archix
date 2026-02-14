package archix_base.common.storage;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * Utility methods for storage operations.
 */
@Component
public class StorageUtils {
    
    private static final Tika tika = new Tika();
    
    /**
     * Allowed MIME types for document upload
     */
    public static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            // PDF
            "application/pdf",
            // Microsoft Office
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // Images
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",
            // Text
            "text/plain",
            "text/csv",
            "text/html",
            "text/xml",
            "application/json",
            // Archives
            "application/zip",
            "application/x-rar-compressed"
    );
    
    /**
     * Maximum file size in bytes (50 MB)
     */
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    
    /**
     * Generate a unique storage path for a document
     *
     * @param organizationId Organization ID
     * @param originalFilename Original filename
     * @return Storage path like "org_123/2026/01/uuid_filename.pdf"
     */
    public static String generateStoragePath(Long organizationId, String originalFilename) {
        LocalDateTime now = LocalDateTime.now();
        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = now.format(DateTimeFormatter.ofPattern("MM"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String sanitizedFilename = sanitizeFilename(originalFilename);
        
        return String.format("org_%d/%s/%s/%s_%s",
                organizationId, year, month, uuid, sanitizedFilename);
    }
    
    /**
     * Generate storage path for document versions
     */
    public static String generateVersionPath(String basePath, int version) {
        int lastDotIndex = basePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return basePath.substring(0, lastDotIndex) + "_v" + version + basePath.substring(lastDotIndex);
        }
        return basePath + "_v" + version;
    }
    
    /**
     * Sanitize filename to remove unsafe characters
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed";
        }
        // Remove path separators and special characters
        return filename
                .replaceAll("[/\\\\:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .toLowerCase();
    }
    
    /**
     * Detect MIME type using Apache Tika
     */
    public static String detectMimeType(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return tika.detect(inputStream, file.getOriginalFilename());
        }
    }
    
    /**
     * Detect MIME type from InputStream
     */
    public static String detectMimeType(InputStream inputStream, String filename) throws IOException {
        return tika.detect(inputStream, filename);
    }
    
    /**
     * Validate that file type is allowed
     */
    public static void validateFileType(String mimeType) {
        if (!ALLOWED_DOCUMENT_TYPES.contains(mimeType)) {
            throw StorageException.invalidFile(
                    "File type not allowed: " + mimeType + 
                    ". Allowed types: " + String.join(", ", ALLOWED_DOCUMENT_TYPES));
        }
    }
    
    /**
     * Validate file size
     */
    public static void validateFileSize(long size) {
        if (size > MAX_FILE_SIZE) {
            throw StorageException.invalidFile(
                    "File too large: " + formatBytes(size) + 
                    ". Maximum allowed: " + formatBytes(MAX_FILE_SIZE));
        }
        if (size == 0) {
            throw StorageException.invalidFile("File is empty");
        }
    }
    
    /**
     * Calculate MD5 checksum of a file
     */
    public static String calculateChecksum(InputStream inputStream) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * Format bytes to human readable string
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }
    
    /**
     * Check if file is an image
     */
    public static boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    /**
     * Check if file is a PDF
     */
    public static boolean isPdf(String mimeType) {
        return "application/pdf".equals(mimeType);
    }
}
