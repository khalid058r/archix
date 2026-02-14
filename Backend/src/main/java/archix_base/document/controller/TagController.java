package archix_base.document.controller;

import archix_base.common.response.ApiResponse;
import archix_base.document.dto.TagDto;
import archix_base.document.entity.Tag;
import archix_base.document.service.TagService;
import archix_base.identity.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Document tag management API")
public class TagController {

    private final TagService tagService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all tags", description = "List all tags for the organization")
    public ResponseEntity<ApiResponse<List<TagDto>>> getAll(
            @RequestHeader(value = "X-Organization-ID") Long organizationId) {

        List<TagDto> tags = tagService.getAllByOrganization(organizationId)
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get tag by ID")
    public ResponseEntity<ApiResponse<TagDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(toDto(tagService.getById(id))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Create tag", description = "Create a new tag for the organization")
    public ResponseEntity<ApiResponse<TagDto>> create(
            @Valid @RequestBody TagDto request,
            @RequestHeader(value = "X-Organization-ID") Long organizationId) {

        Tag tag = tagService.create(request.getName(), request.getColor(),
                request.getDescription(), organizationId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toDto(tag), "Tag created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Update tag")
    public ResponseEntity<ApiResponse<TagDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody TagDto request) {

        Tag updated = tagService.update(id, request.getName(), request.getColor(), request.getDescription());
        return ResponseEntity.ok(ApiResponse.success(toDto(updated), "Tag updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete tag")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private TagDto toDto(Tag tag) {
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setColor(tag.getColor());
        dto.setDescription(tag.getDescription());
        dto.setOrganizationId(tag.getOrganization() != null ? tag.getOrganization().getId() : null);
        dto.setDocumentCount(tag.getDocuments() != null ? tag.getDocuments().size() : 0);
        return dto;
    }
}
