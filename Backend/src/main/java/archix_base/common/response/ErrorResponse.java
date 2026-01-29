package archix_base.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.*;













@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String message;
    private Instant timestamp;
    private Integer status;
    private String error;
    private String path;
    private Map<String, String> fieldErrors;

    public ErrorResponse(String message, Instant timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}







