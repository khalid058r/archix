package archix_base.common.storage;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * MinIO implementation of the StorageService interface.
 * Handles all file operations with MinIO object storage.
 * Only active when storage.local.enabled is false or unset.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.local.enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final StorageProperties properties;
    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        log.info("Initializing MinIO client with endpoint: {}", properties.getEndpoint());

        this.minioClient = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();

        // Create bucket if it doesn't exist
        if (properties.isAutoCreateBucket()) {
            try {
                createBucketIfNotExists(properties.getBucketName());
                log.info("MinIO client initialized successfully");
            } catch (Exception e) {
                if (properties.isFailOnError()) {
                    throw e;
                }
                log.warn("MinIO initialization failed but failOnError=false, continuing without MinIO: {}",
                        e.getMessage());
            }
        } else {
            log.info("MinIO client initialized successfully (bucket auto-creation disabled)");
        }
    }

    /**
     * Create bucket if it doesn't exist
     */
    private void createBucketIfNotExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Created bucket: {}", bucketName);
            } else {
                log.debug("Bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to create/check bucket: {}", bucketName, e);
            throw StorageException.bucketError(bucketName, e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String path) {
        if (file == null || file.isEmpty()) {
            throw StorageException.invalidFile("File is empty or null");
        }

        try (InputStream inputStream = file.getInputStream()) {
            return uploadFile(inputStream, path, file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw StorageException.uploadFailed(path, e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String path, String contentType, long size) {
        try {
            log.debug("Uploading file to path: {}, contentType: {}, size: {}", path, contentType, size);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(path)
                    .stream(inputStream, size, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());

            log.info("File uploaded successfully: {}", path);
            return path;

        } catch (Exception e) {
            log.error("Failed to upload file: {}", path, e);
            throw StorageException.uploadFailed(path, e);
        }
    }

    @Override
    public InputStream downloadFile(String path) {
        try {
            log.debug("Downloading file: {}", path);

            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(path)
                    .build());

        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw StorageException.fileNotFound(path);
            }
            throw StorageException.downloadFailed(path, e);
        } catch (Exception e) {
            log.error("Failed to download file: {}", path, e);
            throw StorageException.downloadFailed(path, e);
        }
    }

    @Override
    public byte[] downloadFileAsBytes(String path) {
        try (InputStream inputStream = downloadFile(path);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();

        } catch (IOException e) {
            throw StorageException.downloadFailed(path, e);
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            log.debug("Deleting file: {}", path);

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(path)
                    .build());

            log.info("File deleted successfully: {}", path);

        } catch (Exception e) {
            log.error("Failed to delete file: {}", path, e);
            throw StorageException.deleteFailed(path, e);
        }
    }

    @Override
    public boolean fileExists(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(path)
                    .build());
            return true;

        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new StorageException("Failed to check file existence: " + path, e);
        } catch (Exception e) {
            throw new StorageException("Failed to check file existence: " + path, e);
        }
    }

    @Override
    public FileMetadata getFileMetadata(String path) {
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(path)
                    .build());

            return FileMetadata.builder()
                    .path(path)
                    .filename(extractFilename(path))
                    .contentType(stat.contentType())
                    .size(stat.size())
                    .checksum(stat.etag())
                    .lastModified(LocalDateTime.ofInstant(
                            stat.lastModified().toInstant(),
                            ZoneId.systemDefault()))
                    .bucket(properties.getBucketName())
                    .build();

        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw StorageException.fileNotFound(path);
            }
            throw new StorageException("Failed to get file metadata: " + path, e);
        } catch (Exception e) {
            throw new StorageException("Failed to get file metadata: " + path, e);
        }
    }

    @Override
    public String generatePresignedUrl(String path, int expirationMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.getBucketName())
                    .object(path)
                    .expiry(expirationMinutes, TimeUnit.MINUTES)
                    .build());

        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", path, e);
            throw new StorageException("Failed to generate presigned URL: " + path, e);
        }
    }

    @Override
    public String generatePresignedUrl(String path) {
        return generatePresignedUrl(path, properties.getUrlExpirationSeconds() / 60);
    }

    @Override
    public String copyFile(String sourcePath, String destinationPath) {
        try {
            log.debug("Copying file from {} to {}", sourcePath, destinationPath);

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(destinationPath)
                    .source(CopySource.builder()
                            .bucket(properties.getBucketName())
                            .object(sourcePath)
                            .build())
                    .build());

            log.info("File copied successfully from {} to {}", sourcePath, destinationPath);
            return destinationPath;

        } catch (Exception e) {
            log.error("Failed to copy file from {} to {}", sourcePath, destinationPath, e);
            throw new StorageException("Failed to copy file", e);
        }
    }

    @Override
    public long getStorageUsage(String prefix) {
        try {
            long totalSize = 0;

            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(properties.getBucketName())
                    .prefix(prefix != null ? prefix : "")
                    .recursive(true)
                    .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                totalSize += item.size();
            }

            return totalSize;

        } catch (Exception e) {
            log.error("Failed to calculate storage usage for prefix: {}", prefix, e);
            throw new StorageException("Failed to calculate storage usage", e);
        }
    }

    /**
     * Extract filename from a path
     */
    private String extractFilename(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}
