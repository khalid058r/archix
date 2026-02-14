package archix_base.common.storage;

import io.minio.MinioClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MinioStorageService.
 * 
 * These tests require a running MinIO instance.
 * They are skipped if MinIO is not available.
 * 
 * To run these tests:
 * 1. Start MinIO: docker run -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address ":9001"
 * 2. Run tests: mvn test -Dtest=MinioStorageServiceIT
 */
@DisplayName("MinioStorageService Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MinioStorageServiceIT {

    private static MinioStorageService storageService;
    private static StorageProperties properties;
    private static boolean minioAvailable = false;
    
    private static final String TEST_BUCKET = "archix-test-bucket";
    private static final String TEST_CONTENT = "Hello, this is test content!";
    private static final String TEST_PATH = "test/integration/test-file.txt";

    @BeforeAll
    static void setUp() {
        properties = new StorageProperties();
        properties.setEndpoint("http://localhost:9000");
        properties.setAccessKey("minioadmin");
        properties.setSecretKey("minioadmin");
        properties.setBucketName(TEST_BUCKET);
        properties.setAutoCreateBucket(true);

        // Check if MinIO is available
        try {
            MinioClient testClient = MinioClient.builder()
                    .endpoint(properties.getEndpoint())
                    .credentials(properties.getAccessKey(), properties.getSecretKey())
                    .build();
            testClient.listBuckets();
            minioAvailable = true;
            
            storageService = new MinioStorageService(properties);
            storageService.init();
        } catch (Exception e) {
            System.out.println("MinIO not available, skipping integration tests: " + e.getMessage());
            minioAvailable = false;
        }
    }

    static boolean isMinioAvailable() {
        return minioAvailable;
    }

    // ============================================
    // Upload Tests
    // ============================================
    @Test
    @Order(1)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should upload file from InputStream")
    void shouldUploadFileFromInputStream() {
        byte[] content = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(content);

        String result = storageService.uploadFile(
                inputStream, 
                TEST_PATH, 
                "text/plain", 
                content.length
        );

        assertEquals(TEST_PATH, result);
        assertTrue(storageService.fileExists(TEST_PATH));
    }

    @Test
    @Order(2)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should upload MultipartFile")
    void shouldUploadMultipartFile() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "multipart-test.txt",
                "text/plain",
                "Multipart content".getBytes(StandardCharsets.UTF_8)
        );

        String path = "test/multipart/multipart-test.txt";
        String result = storageService.uploadFile(multipartFile, path);

        assertEquals(path, result);
        assertTrue(storageService.fileExists(path));
        
        // Cleanup
        storageService.deleteFile(path);
    }

    // ============================================
    // Download Tests
    // ============================================
    @Test
    @Order(3)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should download file as InputStream")
    void shouldDownloadFileAsInputStream() throws Exception {
        try (InputStream inputStream = storageService.downloadFile(TEST_PATH)) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(TEST_CONTENT, content);
        }
    }

    @Test
    @Order(4)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should download file as bytes")
    void shouldDownloadFileAsBytes() {
        byte[] bytes = storageService.downloadFileAsBytes(TEST_PATH);
        String content = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(TEST_CONTENT, content);
    }

    @Test
    @Order(5)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should throw exception for non-existent file")
    void shouldThrowExceptionForNonExistentFile() {
        assertThrows(StorageException.class, () -> {
            storageService.downloadFile("non/existent/file.txt");
        });
    }

    // ============================================
    // Metadata Tests
    // ============================================
    @Test
    @Order(6)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should get file metadata")
    void shouldGetFileMetadata() {
        FileMetadata metadata = storageService.getFileMetadata(TEST_PATH);

        assertNotNull(metadata);
        assertEquals(TEST_PATH, metadata.getPath());
        assertEquals("text/plain", metadata.getContentType());
        assertEquals(TEST_CONTENT.length(), metadata.getSize());
        assertNotNull(metadata.getChecksum());
        assertNotNull(metadata.getLastModified());
    }

    @Test
    @Order(7)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should check file exists")
    void shouldCheckFileExists() {
        assertTrue(storageService.fileExists(TEST_PATH));
        assertFalse(storageService.fileExists("non/existent/path.txt"));
    }

    // ============================================
    // Presigned URL Tests
    // ============================================
    @Test
    @Order(8)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should generate presigned URL")
    void shouldGeneratePresignedUrl() {
        String url = storageService.generatePresignedUrl(TEST_PATH, 60);

        assertNotNull(url);
        assertTrue(url.contains(properties.getEndpoint().replace("http://", "")));
        assertTrue(url.contains(TEST_BUCKET));
    }

    // ============================================
    // Copy Tests
    // ============================================
    @Test
    @Order(9)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should copy file")
    void shouldCopyFile() {
        String destinationPath = "test/copy/copied-file.txt";

        String result = storageService.copyFile(TEST_PATH, destinationPath);

        assertEquals(destinationPath, result);
        assertTrue(storageService.fileExists(destinationPath));
        
        // Verify content
        byte[] copiedContent = storageService.downloadFileAsBytes(destinationPath);
        assertEquals(TEST_CONTENT, new String(copiedContent, StandardCharsets.UTF_8));
        
        // Cleanup
        storageService.deleteFile(destinationPath);
    }

    // ============================================
    // Storage Usage Tests
    // ============================================
    @Test
    @Order(10)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should calculate storage usage")
    void shouldCalculateStorageUsage() {
        long usage = storageService.getStorageUsage("test/");

        assertTrue(usage >= TEST_CONTENT.length());
    }

    // ============================================
    // Delete Tests
    // ============================================
    @Test
    @Order(99)
    @EnabledIf("isMinioAvailable")
    @DisplayName("should delete file")
    void shouldDeleteFile() {
        // First verify file exists
        assertTrue(storageService.fileExists(TEST_PATH));

        // Delete
        storageService.deleteFile(TEST_PATH);

        // Verify deleted
        assertFalse(storageService.fileExists(TEST_PATH));
    }

    // ============================================
    // Error Cases
    // ============================================
    @Test
    @EnabledIf("isMinioAvailable")
    @DisplayName("should throw exception for null file upload")
    void shouldThrowExceptionForNullFileUpload() {
        assertThrows(StorageException.class, () -> {
            storageService.uploadFile((MockMultipartFile) null, "path.txt");
        });
    }

    @Test
    @EnabledIf("isMinioAvailable")
    @DisplayName("should throw exception for empty file upload")
    void shouldThrowExceptionForEmptyFileUpload() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        assertThrows(StorageException.class, () -> {
            storageService.uploadFile(emptyFile, "path.txt");
        });
    }
}
