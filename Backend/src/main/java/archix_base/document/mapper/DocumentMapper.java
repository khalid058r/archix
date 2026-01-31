package archix_base.document.mapper;

import archix_base.document.dto.DocumentDto;
import archix_base.document.entity.Document;

import archix_base.organization.mapper.NamespaceMapper;

public class DocumentMapper {

    public static DocumentDto toDto(Document doc) {
        if (doc == null)
            return null;
        DocumentDto dto = new DocumentDto();
        dto.setId(doc.getId());
        dto.setName(doc.getName());
        dto.setFileName(doc.getFileName());
        dto.setFileSize(doc.getFileSize());
        dto.setMimeType(doc.getMimeType());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setUpdatedAt(doc.getUpdatedAt());
        dto.setStatus(doc.getStatus() != null ? doc.getStatus().name() : "DRAFT");
        dto.setCreatedById(doc.getCreatedBy() != null ? doc.getCreatedBy().getId() : null);
        dto.setCreatedByName(doc.getCreatedBy() != null ? doc.getCreatedBy().getFullName() : "Unknown");
        dto.setParentId(doc.getParent() != null ? doc.getParent().getId() : null);
        // Map full Namespace object for Frontend display
        if (doc.getParent() != null) {
            dto.setNamespace(NamespaceMapper.toDto(doc.getParent()));
        }
        dto.setOrganizationId(doc.getOrganization() != null ? doc.getOrganization().getId() : null);
        return dto;
    }

    public static Document toEntity(DocumentDto dto) {
        if (dto == null)
            return null;
        Document doc = new Document();
        doc.setId(dto.getId());
        doc.setName(dto.getName());
        doc.setFileName(dto.getFileName());
        doc.setFileSize(dto.getFileSize());
        doc.setMimeType(dto.getMimeType());
        doc.setUpdatedAt(dto.getUpdatedAt());
        // createdBy et parent sont
        // gÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©s
        // par le service lors de la persistance
        return doc;
    }

    public static archix_base.document.dto.DocumentVersionDto toVersionDto(
            archix_base.document.entity.DocumentVersion version) {
        if (version == null)
            return null;
        try {
            archix_base.document.dto.DocumentVersionDto dto = new archix_base.document.dto.DocumentVersionDto();
            dto.setId(version.getId());
            dto.setVersionNumber(version.getVersionNumber());
            dto.setFileName(version.getFileName());
            dto.setMimeType(version.getMimeType());
            dto.setFileSize(version.getFileSize());
            dto.setArchivedAt(version.getArchivedAt());
            dto.setArchivedBy(version.getArchivedBy() != null ? version.getArchivedBy().getFullName() : "System");
            return dto;
        } catch (Exception e) {
            System.err.println("ERROR mapping DocumentVersion ID " + version.getId() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
