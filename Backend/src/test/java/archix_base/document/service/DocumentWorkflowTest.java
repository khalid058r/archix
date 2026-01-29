package archix_base.document.service;

import archix_base.document.entity.Document;
import archix_base.document.entity.DocumentStatus;
import archix_base.document.repo.DocumentRepo;
import archix_base.document.repo.DocumentVersionRepo;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.repo.NamespaceRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DocumentWorkflowTest {

    @Mock
    private DocumentRepo documentRepository;

    @Mock
    private PermissionRepo permissionRepository;

    @Mock
    private NamespaceRepo namespaceRepository;

    @Mock
    private DocumentVersionRepo documentVersionRepository;

    @Mock
    private UserRepo userRepository;

    @InjectMocks
    private DocumentService documentService;

    private Document draftDoc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        draftDoc = new Document();
        draftDoc.setId(1L);
        draftDoc.setFileName("test.pdf");
        draftDoc.setStatus(DocumentStatus.DRAFT);
        draftDoc.setVersion(1);
    }

    @Test
    void testSubmitForReview_Success() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(draftDoc));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        Document updated = documentService.submitForReview(1L);

        assertEquals(DocumentStatus.PENDING_REVIEW, updated.getStatus());
        verify(documentRepository).save(draftDoc);
    }

    @Test
    void testSubmitForReview_Fail_WrongStatus() {
        draftDoc.setStatus(DocumentStatus.APPROVED);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(draftDoc));

        assertThrows(IllegalStateException.class, () -> documentService.submitForReview(1L));
    }

    @Test
    void testFullWorkflowHappyPath() {
        // DRAFT -> PENDING
        when(documentRepository.findById(1L)).thenReturn(Optional.of(draftDoc));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        Document pending = documentService.submitForReview(1L);
        assertEquals(DocumentStatus.PENDING_REVIEW, pending.getStatus());

        // PENDING -> IN_REVIEW
        Document inReview = documentService.startReview(1L);
        assertEquals(DocumentStatus.IN_REVIEW, inReview.getStatus());

        // IN_REVIEW -> APPROVED
        Document approved = documentService.approve(1L);
        assertEquals(DocumentStatus.APPROVED, approved.getStatus());

        // APPROVED -> PUBLISHED
        Document published = documentService.publish(1L);
        assertEquals(DocumentStatus.PUBLISHED, published.getStatus());
    }

    @Test
    void testRejectWorkflow() {
        // Prepare Doc as IN_REVIEW
        draftDoc.setStatus(DocumentStatus.IN_REVIEW);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(draftDoc));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        Document rejected = documentService.reject(1L, "Bad content");

        assertEquals(DocumentStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void testUpdateCreatesVersion() {
        // Mock existing doc
        Document existing = new Document();
        existing.setId(2L);
        existing.setFileName("old.pdf");
        existing.setVersion(1);
        existing.setFileSize(100L);
        existing.setStatus(DocumentStatus.PUBLISHED);

        when(documentRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        // Update data
        Document updateData = new Document();
        updateData.setFileName("new.pdf"); // Change name

        // Call update
        Document updated = documentService.update(2L, updateData, null);

        // Verify version incremented
        assertEquals(2, updated.getVersion());
        // Verify status reset to DRAFT
        assertEquals(DocumentStatus.DRAFT, updated.getStatus());

        // Verify version was saved
        verify(documentVersionRepository).save(any(archix_base.document.entity.DocumentVersion.class));
    }
}
