package archix_base.common.repo;

import archix_base.common.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepo extends JpaRepository<SystemSetting, Long> {

    Optional<SystemSetting> findByKeyAndOrganizationId(String key, Long organizationId);

    Optional<SystemSetting> findByKeyAndOrganizationIdIsNull(String key);

    List<SystemSetting> findAllByOrganizationId(Long organizationId);

    List<SystemSetting> findAllByOrganizationIdIsNull();

    boolean existsByKeyAndOrganizationId(String key, Long organizationId);
}
