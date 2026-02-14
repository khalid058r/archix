package archix_base.common.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StorageUtils utility class.
 */
@DisplayName("StorageUtils Tests")
class StorageUtilsTest {

    // ============================================
    // sanitizeFilename Tests
    // ============================================
    @Nested
    @DisplayName("sanitizeFilename()")
    class SanitizeFilenameTests {

        @Test
        @DisplayName("should return lowercase filename")
        void shouldReturnLowercaseFilename() {
            String result = StorageUtils.sanitizeFilename("MyDocument.PDF");
            assertEquals("mydocument.pdf", result);
        }

        @Test
        @DisplayName("should replace spaces with underscores")
        void shouldReplaceSpacesWithUnderscores() {
            String result = StorageUtils.sanitizeFilename("my document file.pdf");
            assertEquals("my_document_file.pdf", result);
        }

        @Test
        @DisplayName("should remove special characters")
        void shouldRemoveSpecialCharacters() {
            String result = StorageUtils.sanitizeFilename("file:name*test?.pdf");
            assertEquals("file_name_test_.pdf", result);
        }

        @Test
        @DisplayName("should handle path separators")
        void shouldHandlePathSeparators() {
            String result = StorageUtils.sanitizeFilename("path/to\\file.pdf");
            assertEquals("path_to_file.pdf", result);
        }

        @Test
        @DisplayName("should collapse multiple underscores")
        void shouldCollapseMultipleUnderscores() {
            String result = StorageUtils.sanitizeFilename("file___name.pdf");
            assertEquals("file_name.pdf", result);
        }

        @Test
        @DisplayName("should return 'unnamed' for null input")
        void shouldReturnUnnamedForNull() {
            String result = StorageUtils.sanitizeFilename(null);
            assertEquals("unnamed", result);
        }

        @Test
        @DisplayName("should return 'unnamed' for empty input")
        void shouldReturnUnnamedForEmpty() {
            String result = StorageUtils.sanitizeFilename("");
            assertEquals("unnamed", result);
        }
    }

    // ============================================
    // generateStoragePath Tests
    // ============================================
    @Nested
    @DisplayName("generateStoragePath()")
    class GenerateStoragePathTests {

        @Test
        @DisplayName("should generate path with organization prefix")
        void shouldGeneratePathWithOrgPrefix() {
            String result = StorageUtils.generateStoragePath(123L, "document.pdf");
            assertTrue(result.startsWith("org_123/"));
        }

        @Test
        @DisplayName("should include year and month in path")
        void shouldIncludeYearAndMonthInPath() {
            String result = StorageUtils.generateStoragePath(1L, "test.pdf");
            // Format: org_1/YYYY/MM/uuid_test.pdf
            assertTrue(result.matches("org_1/\\d{4}/\\d{2}/[a-f0-9]{8}_test\\.pdf"));
        }

        @Test
        @DisplayName("should sanitize filename in path")
        void shouldSanitizeFilenameInPath() {
            String result = StorageUtils.generateStoragePath(1L, "My Document.PDF");
            assertTrue(result.endsWith("my_document.pdf"));
        }
    }

    // ============================================
    // generateVersionPath Tests
    // ============================================
    @Nested
    @DisplayName("generateVersionPath()")
    class GenerateVersionPathTests {

        @Test
        @DisplayName("should add version suffix before extension")
        void shouldAddVersionSuffixBeforeExtension() {
            String result = StorageUtils.generateVersionPath("path/to/file.pdf", 2);
            assertEquals("path/to/file_v2.pdf", result);
        }

        @Test
        @DisplayName("should handle files without extension")
        void shouldHandleFilesWithoutExtension() {
            String result = StorageUtils.generateVersionPath("path/to/file", 3);
            assertEquals("path/to/file_v3", result);
        }

        @Test
        @DisplayName("should handle multiple dots in filename")
        void shouldHandleMultipleDots() {
            String result = StorageUtils.generateVersionPath("file.backup.pdf", 1);
            assertEquals("file.backup_v1.pdf", result);
        }
    }

    // ============================================
    // validateFileType Tests
    // ============================================
    @Nested
    @DisplayName("validateFileType()")
    class ValidateFileTypeTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "application/pdf",
            "image/jpeg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "application/json"
        })
        @DisplayName("should accept allowed MIME types")
        void shouldAcceptAllowedMimeTypes(String mimeType) {
            assertDoesNotThrow(() -> StorageUtils.validateFileType(mimeType));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/x-executable",
            "application/x-msdownload",
            "video/mp4",
            "audio/mpeg"
        })
        @DisplayName("should reject disallowed MIME types")
        void shouldRejectDisallowedMimeTypes(String mimeType) {
            StorageException exception = assertThrows(
                StorageException.class,
                () -> StorageUtils.validateFileType(mimeType)
            );
            assertTrue(exception.getMessage().contains("File type not allowed"));
        }
    }

    // ============================================
    // validateFileSize Tests
    // ============================================
    @Nested
    @DisplayName("validateFileSize()")
    class ValidateFileSizeTests {

        @Test
        @DisplayName("should accept files within limit")
        void shouldAcceptFilesWithinLimit() {
            assertDoesNotThrow(() -> StorageUtils.validateFileSize(10 * 1024 * 1024)); // 10 MB
        }

        @Test
        @DisplayName("should reject files exceeding limit")
        void shouldRejectFilesExceedingLimit() {
            long tooLarge = 60 * 1024 * 1024; // 60 MB
            StorageException exception = assertThrows(
                StorageException.class,
                () -> StorageUtils.validateFileSize(tooLarge)
            );
            assertTrue(exception.getMessage().contains("File too large"));
        }

        @Test
        @DisplayName("should reject empty files")
        void shouldRejectEmptyFiles() {
            StorageException exception = assertThrows(
                StorageException.class,
                () -> StorageUtils.validateFileSize(0)
            );
            assertTrue(exception.getMessage().contains("empty"));
        }
    }

    // ============================================
    // formatBytes Tests
    // ============================================
    @Nested
    @DisplayName("formatBytes()")
    class FormatBytesTests {

        @ParameterizedTest
        @CsvSource({
            "0, 0 B",
            "500, 500 B",
            "1024, 1.0 KB",
            "1536, 1.5 KB",
            "1048576, 1.0 MB",
            "1073741824, 1.0 GB"
        })
        @DisplayName("should format bytes correctly")
        void shouldFormatBytesCorrectly(long bytes, String expected) {
            assertEquals(expected, StorageUtils.formatBytes(bytes));
        }
    }

    // ============================================
    // getFileExtension Tests
    // ============================================
    @Nested
    @DisplayName("getFileExtension()")
    class GetFileExtensionTests {

        @Test
        @DisplayName("should extract extension")
        void shouldExtractExtension() {
            assertEquals("pdf", StorageUtils.getFileExtension("document.pdf"));
        }

        @Test
        @DisplayName("should return lowercase extension")
        void shouldReturnLowercaseExtension() {
            assertEquals("pdf", StorageUtils.getFileExtension("document.PDF"));
        }

        @Test
        @DisplayName("should handle no extension")
        void shouldHandleNoExtension() {
            assertEquals("", StorageUtils.getFileExtension("document"));
        }

        @Test
        @DisplayName("should handle null")
        void shouldHandleNull() {
            assertEquals("", StorageUtils.getFileExtension(null));
        }

        @Test
        @DisplayName("should handle multiple dots")
        void shouldHandleMultipleDots() {
            assertEquals("gz", StorageUtils.getFileExtension("archive.tar.gz"));
        }
    }

    // ============================================
    // isImage / isPdf Tests
    // ============================================
    @Nested
    @DisplayName("Type checking methods")
    class TypeCheckingTests {

        @Test
        @DisplayName("isImage should return true for image types")
        void isImageShouldReturnTrueForImages() {
            assertTrue(StorageUtils.isImage("image/jpeg"));
            assertTrue(StorageUtils.isImage("image/png"));
            assertTrue(StorageUtils.isImage("image/gif"));
        }

        @Test
        @DisplayName("isImage should return false for non-image types")
        void isImageShouldReturnFalseForNonImages() {
            assertFalse(StorageUtils.isImage("application/pdf"));
            assertFalse(StorageUtils.isImage("text/plain"));
            assertFalse(StorageUtils.isImage(null));
        }

        @Test
        @DisplayName("isPdf should return true for PDF")
        void isPdfShouldReturnTrueForPdf() {
            assertTrue(StorageUtils.isPdf("application/pdf"));
        }

        @Test
        @DisplayName("isPdf should return false for non-PDF")
        void isPdfShouldReturnFalseForNonPdf() {
            assertFalse(StorageUtils.isPdf("image/jpeg"));
            assertFalse(StorageUtils.isPdf(null));
        }
    }
}
