package archix_base.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
public class DiagnosticController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-check")
    public ResponseEntity<?> checkDb() {
        try {
            // Check count
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM resources", Integer.class);

            // Check columns
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'resources'");

            // Check sample data (specifically properties that might fail mapping)
            List<Map<String, Object>> sample = jdbcTemplate.queryForList("SELECT * FROM resources LIMIT 5");

            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "count", count,
                    "columns", columns,
                    "sample", sample));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
