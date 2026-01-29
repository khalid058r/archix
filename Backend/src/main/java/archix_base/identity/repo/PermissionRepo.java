package archix_base.identity.repo;

import archix_base.document.entity.Resource;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.PermissionType;
import archix_base.identity.entity.User;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepo extends JpaRepository<Permission, Long> {

    // Find permissions by user
    List<Permission> findByGrantedTo(User user);

    List<Permission> findByGrantedToId(Long userId);

    // Find permissions by resource
    List<Permission> findByAppliesTo(Resource resource);

    List<Permission> findByAppliesToId(Long resourceId);

    // Find permissions granted by a user
    List<Permission> findByGrantedBy(User user);

    List<Permission> findByGrantedById(Long userId);

    // Find by permission type
    List<Permission> findByType(PermissionType type);

    // Find specific permission for user on resource
    @Query("SELECT p FROM Permission p WHERE p.grantedTo.id = :userId AND p.appliesTo.id = :resourceId")
    List<Permission> findByUserAndResource(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    // Find specific permission by user, resource and type
    Optional<Permission> findByGrantedToIdAndAppliesToIdAndType(Long userId, Long resourceId, PermissionType type);

    // Check if permission exists
    boolean existsByGrantedToIdAndAppliesToIdAndType(Long userId, Long resourceId, PermissionType type);

    // Check if user has any permission on resource
    boolean existsByGrantedToIdAndAppliesToId(Long userId, Long resourceId);

    // Delete all permissions for a resource
    @Transactional
    @Modifying
    @Query("DELETE FROM Permission p WHERE p.appliesTo.id = :resourceId")
    void deleteByAppliesToId(@Param("resourceId") Long resourceId);

    // Delete all permissions for a user
    @Transactional
    @Modifying
    @Query("DELETE FROM Permission p WHERE p.grantedTo.id = :userId")
    void deleteByGrantedToId(@Param("userId") Long userId);

    // Count permissions for a resource
    // Find permissions for a user on a resource (used for direct permission check)
    List<Permission> findByGrantedToIdAndAppliesToId(Long grantedToId, Long appliesToId);

    long countByAppliesToId(Long resourceId);
}
