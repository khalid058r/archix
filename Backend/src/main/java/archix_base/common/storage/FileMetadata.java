package archix_base.common.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Metadata information about a stored file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    
    /**
     * Storage path of the file
     */
    private String path;
    
    /**
     * Original filename
     */
    private String filename;
    
    /**
     * MIME content type
     */
    private String contentType;
    
    /**
     * File size in bytes
     */
    private long size;
    
    /**
     * MD5 checksum/ETag
     */
    private String checksum;
    
    /**
     * Last modified timestamp
     */
    private LocalDateTime lastModified;
    
    /**
     * Bucket name where file is stored
     */
    private String bucket;
}
