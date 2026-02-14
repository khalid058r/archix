package archix_base.common.storage;

/**
 * Exception thrown when storage operations fail.
 */
public class StorageException extends RuntimeException {
    
    public StorageException(String message) {
        super(message);
    }
    
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * File not found in storage
     */
    public static StorageException fileNotFound(String path) {
        return new StorageException("File not found: " + path);
    }
    
    /**
     * Upload failed
     */
    public static StorageException uploadFailed(String path, Throwable cause) {
        return new StorageException("Failed to upload file: " + path, cause);
    }
    
    /**
     * Download failed
     */
    public static StorageException downloadFailed(String path, Throwable cause) {
        return new StorageException("Failed to download file: " + path, cause);
    }
    
    /**
     * Delete failed
     */
    public static StorageException deleteFailed(String path, Throwable cause) {
        return new StorageException("Failed to delete file: " + path, cause);
    }
    
    /**
     * Invalid file
     */
    public static StorageException invalidFile(String reason) {
        return new StorageException("Invalid file: " + reason);
    }
    
    /**
     * Bucket operation failed
     */
    public static StorageException bucketError(String bucket, Throwable cause) {
        return new StorageException("Bucket operation failed for: " + bucket, cause);
    }
}
