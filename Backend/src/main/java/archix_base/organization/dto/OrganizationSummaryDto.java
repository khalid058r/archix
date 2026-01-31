package archix_base.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSummaryDto {
    private Long id;
    private String name;
    private String planType;
    private boolean isOwner;
}
