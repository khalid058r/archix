package archix_base.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatsDTO {
    private long totalDocuments;
    private long drafts;
    private long inReview;
    private long published;
}
