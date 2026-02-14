package archix_base.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagDto {
    private Long id;

    @NotBlank(message = "Tag name is required")
    @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters")
    private String name;

    private String color;
    private String description;
    private Long organizationId;
    private int documentCount;
}
