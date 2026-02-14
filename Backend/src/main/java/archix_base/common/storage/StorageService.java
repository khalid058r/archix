package archix_base.common.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Interface for file storage operations.
 * Abstracts the storage backend (MinIO, S3, local filesystem, etc.)
 */
public interface StorageService {
    
    /**
     * Upload a file to storage
     *
     * @param file The file to upload
     * @param path The storage path (e.g., "documents/2026/01/file.pdf")
     * @return The full storage path of the uploaded file
     */
    String uploadFile(MultipartFile file, String path);
    
    /**
     * Upload a file from an InputStream
     *
     * @param inputStream The input stream containing file data
     * @param path The storage path
     * @param contentType The MIME type of the file
     * @param size The size of the file in bytes
     * @return The full storage path
     */
    String uploadFile(InputStream inputStream, String path, String contentType, long size);
    
    /**
     * Download a file as an InputStream
     *
     * @param path The storage path of the file
     * @return InputStream of the file content
     */
    InputStream downloadFile(String path);
    
    /**
     * Download a file as a byte array (use with caution for large files)
     *
     * @param path The storage path
     * @return byte array of file content
     */
    byte[] downloadFileAsBytes(String path);
    
    /**
     * Delete a file from storage
     *
     * @param path The storage path of the file to delete
     */
    void deleteFile(String path);
    
    /**
     * Check if a file exists
     *
     * @param path The storage path
     * @return true if the file exists
     */
    boolean fileExists(String path);
    
    /**
     * Get file metadata (size, content type, etc.)
     *
     * @param path The storage path
     * @return FileMetadata object
     */
    FileMetadata getFileMetadata(String path);
    
    /**
     * Generate a pre-signed URL for temporary direct access
     *
     * @param path The storage path
     * @param expirationMinutes URL validity in minutes
     * @return Pre-signed URL string
     */
    String generatePresignedUrl(String path, int expirationMinutes);
    
    /**
     * Generate a pre-signed URL with default expiration (1 hour)
     *
     * @param path The storage path
     * @return Pre-signed URL string
     */
    String generatePresignedUrl(String path);
    
    /**
     * Copy a file within storage
     *
     * @param sourcePath Source file path
     * @param destinationPath Destination file path
     * @return The destination path
     */
    String copyFile(String sourcePath, String destinationPath);
    
    /**
     * Get the total storage usage in bytes
     *
     * @param prefix Optional path prefix to calculate usage for a specific folder
     * @return Total bytes used
     */
    long getStorageUsage(String prefix);
}
