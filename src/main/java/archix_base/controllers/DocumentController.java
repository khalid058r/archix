package archix_base.controllers;

import archix_base.dto.DocumentDto;
import archix_base.entities.Document;
import archix_base.mapper.DocumentMapper;
import archix_base.services.DocumentService;
import archix_base.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping
    public List<DocumentDto> getAll() {
        return documentService.getAll().stream()
                .map(DocumentMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public DocumentDto getById(@PathVariable Long id) {
        Document doc = documentService.getById(id);
        return DocumentMapper.toDto(doc);
    }

    @PostMapping
    public DocumentDto create(@RequestBody DocumentDto dto) {
        Document doc = DocumentMapper.toEntity(dto);
        Document saved = documentService.create(doc, dto.getCreatedById(), dto.getParentId());
        return DocumentMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public DocumentDto update(@PathVariable Long id, @RequestBody DocumentDto dto) {
        Document doc = DocumentMapper.toEntity(dto);
        Document updated = documentService.update(id, doc, dto.getParentId());
        return DocumentMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        documentService.delete(id);
    }

    // Endpoint pour lister les documents d'un namespace parent
    @GetMapping("/namespace/{namespaceId}")
    public List<DocumentDto> getByNamespace(@PathVariable Long namespaceId) {
        return documentService.getDocumentsByNamespace(namespaceId)
                .stream()
                .map(DocumentMapper::toDto)
                .collect(Collectors.toList());
    }
}