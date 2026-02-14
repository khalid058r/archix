package archix_base.organization.entity;

/**
 * SaaS plan types with their quotas and limits.
 */
public enum PlanType {
    
    FREE("Free", 1_073_741_824L, 5, 100),           // 1 GB, 5 users, 100 docs
    STARTER("Starter", 10_737_418_240L, 25, 1000),  // 10 GB, 25 users, 1000 docs
    PROFESSIONAL("Professional", 107_374_182_400L, 100, 10000), // 100 GB, 100 users, 10000 docs
    ENTERPRISE("Enterprise", null, null, null);     // Unlimited
    
    private final String displayName;
    private final Long defaultStorageQuotaBytes;
    private final Integer defaultMaxUsers;
    private final Integer defaultMaxDocuments;
    
    PlanType(String displayName, Long storage, Integer users, Integer docs) {
        this.displayName = displayName;
        this.defaultStorageQuotaBytes = storage;
        this.defaultMaxUsers = users;
        this.defaultMaxDocuments = docs;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Long getDefaultStorageQuotaBytes() {
        return defaultStorageQuotaBytes;
    }
    
    public Integer getDefaultMaxUsers() {
        return defaultMaxUsers;
    }
    
    public Integer getDefaultMaxDocuments() {
        return defaultMaxDocuments;
    }
    
    /**
     * Format storage quota as human-readable string.
     */
    public String getStorageQuotaFormatted() {
        if (defaultStorageQuotaBytes == null) return "Unlimited";
        long gb = defaultStorageQuotaBytes / (1024 * 1024 * 1024);
        return gb + " GB";
    }
    
    /**
     * Format max users as string.
     */
    public String getMaxUsersFormatted() {
        if (defaultMaxUsers == null) return "Unlimited";
        return defaultMaxUsers.toString();
    }
}
