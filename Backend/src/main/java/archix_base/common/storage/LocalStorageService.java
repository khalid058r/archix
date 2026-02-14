package archix_base.common.storage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

/**
 * Local filesystem implementation of the StorageService interface.
 * Used as a fallback when MinIO is not available for development.
 * 
 * Enable this by setting: storage.local.enabled=true in application.properties
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.local.enabled", havingValue = "true")
@Primary
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;
    private Path rootPath;

    @PostConstruct
    public void init() {
        // Use system temp directory or configured path
        String localPath = System.getProperty("java.io.tmpdir") + "/archix-storage";
        this.rootPath = Paths.get(localPath);

        try {
            Files.createDirectories(rootPath);
            log.info("LocalStorageService initialized at: {}", rootPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create local storage directory: {}", e.getMessage());
            throw new RuntimeException("Cannot initialize local storage", e);
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
            Path filePath = rootPath.resolve(path);

            // Create parent directories if needed
            Files.createDirectories(filePath.getParent());

            // Copy the file
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File uploaded to local storage: {}", filePath.toAbsolutePath());
            return path;

        } catch (IOException e) {
            log.error("Failed to upload file locally: {}", path, e);
            throw StorageException.uploadFailed(path, e);
        }
    }

    @Override
    public InputStream downloadFile(String path) {
        try {
            Path filePath = rootPath.resolve(path);

            if (!Files.exists(filePath)) {
                throw StorageException.fileNotFound(path);
            }

            return new FileInputStream(filePath.toFile());

        } catch (FileNotFoundException e) {
            throw StorageException.fileNotFound(path);
        }
    }

    @Override
    public byte[] downloadFileAsBytes(String path) {
        try {
            Path filePath = rootPath.resolve(path);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw StorageException.downloadFailed(path, e);
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            Path filePath = rootPath.resolve(path);
            Files.deleteIfExists(filePath);
            log.info("File deleted from local storage: {}", path);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", path, e);
            throw StorageException.deleteFailed(path, e);
        }
    }

    @Override
    public boolean fileExists(String path) {
        return Files.exists(rootPath.resolve(path));
    }

    @Override
    public FileMetadata getFileMetadata(String path) {
        try {
            Path filePath = rootPath.resolve(path);

            if (!Files.exists(filePath)) {
                throw StorageException.fileNotFound(path);
            }

            return FileMetadata.builder()
                    .path(path)
                    .filename(filePath.getFileName().toString())
                    .contentType(Files.probeContentType(filePath))
                    .size(Files.size(filePath))
                    .checksum(calculateChecksum(filePath))
                    .lastModified(LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(filePath).toInstant(),
                            ZoneId.systemDefault()))
                    .bucket("local")
                    .build();

        } catch (IOException e) {
            throw new StorageException("Failed to get file metadata: " + path, e);
        }
    }

    @Override
    public String generatePresignedUrl(String path, int expirationMinutes) {
        // For local storage, return a file:// URL (not secure, for dev only)
        Path filePath = rootPath.resolve(path);
        log.warn("Generating local file URL (DEV ONLY): {}", filePath);
        return "file://" + filePath.toAbsolutePath().toString().replace("\\", "/");
    }

    @Override
    public String generatePresignedUrl(String path) {
        return generatePresignedUrl(path, 60);
    }

    @Override
    public String copyFile(String sourcePath, String destinationPath) {
        try {
            Path source = rootPath.resolve(sourcePath);
            Path destination = rootPath.resolve(destinationPath);

            Files.createDirectories(destination.getParent());
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

            log.info("File copied from {} to {}", sourcePath, destinationPath);
            return destinationPath;

        } catch (IOException e) {
            throw new StorageException("Failed to copy file", e);
        }
    }

    @Override
    public long getStorageUsage(String prefix) {
        try {
            Path searchPath = prefix != null ? rootPath.resolve(prefix) : rootPath;

            if (!Files.exists(searchPath)) {
                return 0;
            }

            try (Stream<Path> walk = Files.walk(searchPath)) {
                return walk
                        .filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                return 0;
                            }
                        })
                        .sum();
            }

        } catch (IOException e) {
            log.error("Failed to calculate storage usage: {}", e.getMessage());
            return 0;
        }
    }

    private String calculateChecksum(Path filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] data = Files.readAllBytes(filePath);
            byte[] hash = md.digest(data);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            return null;
        }
    }
}
