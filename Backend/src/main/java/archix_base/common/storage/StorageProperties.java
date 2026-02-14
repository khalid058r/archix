package archix_base.common.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for MinIO storage.
 * Values are loaded from application.properties with prefix "minio."
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class StorageProperties {
    
    /**
     * MinIO server endpoint URL
     * Example: http://localhost:9000
     */
    private String endpoint = "http://localhost:9000";
    
    /**
     * MinIO access key (username)
     */
    private String accessKey = "minioadmin";
    
    /**
     * MinIO secret key (password)
     */
    private String secretKey = "minioadmin";
    
    /**
     * Default bucket name for document storage
     */
    private String bucketName = "archix-documents";
    
    /**
     * Whether to automatically create the bucket if it doesn't exist
     */
    private boolean autoCreateBucket = true;
    
    /**
     * URL expiration time for pre-signed URLs (in seconds)
     * Default: 1 hour
     */
    private int urlExpirationSeconds = 3600;
    
    /**
     * Whether to fail application startup if MinIO connection fails.
     * Set to false for development to allow app to start without MinIO.
     */
    private boolean failOnError = true;
}
