package archix_base.document.controller;

import archix_base.document.dto.DocumentDto;
import archix_base.document.entity.DocumentStatus;
import archix_base.document.service.DocumentService;
import archix_base.identity.entity.User;
import archix_base.common.response.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DocumentControllerReproTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @Test
    public void testGetAll_Reproduction() {
        // Mock User
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");

        // Mock Service response
        Page<archix_base.document.entity.Document> emptyPage = new PageImpl<>(Collections.emptyList());
        // We need to mock the service call. Note that the controller calls
        // map(DocumentMapper::toDto)
        // But the service returns Page<Document>.
        // Wait, DocumentService.getAll returns Page<Document>.

        when(documentService.getAll(any(Pageable.class), any(), anyLong(), anyLong()))
                .thenReturn(emptyPage);

        // Call Controller
        try {
            ResponseEntity<PageResponse<DocumentDto>> response = documentController.getAll(
                    0, 20, "createdAt", "desc", null, 1L, mockUser);

            assertNotNull(response);
            assertEquals(200, response.getStatusCodeValue());
            System.out.println("Test passed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            fail("Controller threw exception: " + e.getMessage());
        }
    }
}
