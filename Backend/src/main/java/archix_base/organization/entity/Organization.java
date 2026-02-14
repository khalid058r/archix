package archix_base.organization.entity;

import archix_base.identity.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Organization entity representing a tenant in the SaaS system.
 * Each organization has its own storage quota, user limits, and plan.
 */















@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String phone;
    private String email;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // SaaS Plan & Quotas
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan = PlanType.FREE;
    
    @Column(nullable = false)
    private Long storageUsedBytes = 0L;
    
    private Long storageQuotaBytes; // null = unlimited (for enterprise)
    
    private Integer maxUsers; // null = unlimited
    
    private Integer currentUserCount = 0;
    
    // Organization settings stored as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String settings;
    
    // Audit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
    
    // Status
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private LocalDateTime suspendedAt;
    private String suspensionReason;
    
    @OneToMany(mappedBy = "organization")
    private List<Department> departments;
    
    // ============ Helper Methods ============
    
    /**
     * Check if organization can add more users.
     */
    public boolean canAddUser() {
        if (maxUsers == null) return true;
        return currentUserCount < maxUsers;
    }
    
    /**
     * Check if organization has storage space available.
     */
    public boolean hasStorageSpace(long additionalBytes) {
        if (storageQuotaBytes == null) return true;
        return (storageUsedBytes + additionalBytes) <= storageQuotaBytes;
    }
    
    /**
     * Add to storage usage.
     */
    public void addStorageUsage(long bytes) {
        this.storageUsedBytes = (this.storageUsedBytes == null ? 0L : this.storageUsedBytes) + bytes;
    }
    
    /**
     * Remove from storage usage.
     */
    public void removeStorageUsage(long bytes) {
        this.storageUsedBytes = Math.max(0, (this.storageUsedBytes == null ? 0L : this.storageUsedBytes) - bytes);
    }
    
    /**
     * Get storage usage percentage.
     */
    public Double getStorageUsagePercentage() {
        if (storageQuotaBytes == null || storageQuotaBytes == 0) return 0.0;
        return (storageUsedBytes.doubleValue() / storageQuotaBytes.doubleValue()) * 100;
    }
    
    /**
     * Increment user count.
     */
    public void incrementUserCount() {
        this.currentUserCount = (this.currentUserCount == null ? 0 : this.currentUserCount) + 1;
    }
    
    /**
     * Decrement user count.
     */
    public void decrementUserCount() {
        this.currentUserCount = Math.max(0, (this.currentUserCount == null ? 0 : this.currentUserCount) - 1);
    }
}