package archix_base.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsDto {
    private Long id;
    private String name;
    private int memberCount;
    private long documentCount;
    private long pendingDocumentsCount;
    private long storageUsedBytes; // Placeholder for now
}
