package archix_base.audit.repo;

import archix_base.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepo extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<AuditLog> findAllByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countByActionSince(@Param("since") LocalDateTime since);

    @Query("SELECT CAST(a.timestamp AS date), COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since GROUP BY CAST(a.timestamp AS date) ORDER BY CAST(a.timestamp AS date)")
    List<Object[]> countByDaySince(@Param("since") LocalDateTime since);

    @Query("SELECT a.userId, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since GROUP BY a.userId ORDER BY COUNT(a) DESC")
    List<Object[]> countByUserSince(@Param("since") LocalDateTime since);

    long countByTimestampAfter(LocalDateTime since);
}
