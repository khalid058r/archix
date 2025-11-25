package archix_base.mapper;

import archix_base.entities.Document;
import archix_base.dto.DocumentDto;

public class DocumentMapper {

    public static DocumentDto toDto(Document doc) {
        if (doc == null) return null;
        DocumentDto dto = new DocumentDto();
        dto.setId(doc.getId());
        dto.setName(doc.getName());
        dto.setFileName(doc.getFileName());
        dto.setFileSize(doc.getFileSize());
        dto.setMimeType(doc.getMimeType());
        dto.setUpdatedAt(doc.getUpdatedAt());
        dto.setCreatedById(doc.getCreatedBy() != null ? doc.getCreatedBy().getId() : null);
        dto.setParentId(doc.getParent() != null ? doc.getParent().getId() : null);
        return dto;
    }

    public static Document toEntity(DocumentDto dto) {
        if (dto == null) return null;
        Document doc = new Document();
        doc.setId(dto.getId());
        doc.setName(dto.getName());
        doc.setFileName(dto.getFileName());
        doc.setFileSize(dto.getFileSize());
        doc.setMimeType(dto.getMimeType());
        doc.setUpdatedAt(dto.getUpdatedAt());
        // createdBy et parent sont gérés par le service lors de la persistance
        return doc;
    }
}