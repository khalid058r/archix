package archix_base.organization.entity;

import archix_base.document.entity.Document;
import archix_base.document.entity.Resource;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Namespace (folder) entity for organizing documents hierarchically.
 * Supports materialized paths for efficient tree queries.
 */
@Entity
@Table(name = "namespace")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Namespace extends Resource {

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> children = new ArrayList<>();
    
    // ============ Materialized Path ============
    
    /**
     * Full path from root (e.g., "/root/folder1/subfolder")
     * Pre-computed for efficient querying.
     */
    @Column(length = 2000)
    private String fullPath;
    
    /**
     * Depth level in the tree (root = 0)
     */
    @Column(nullable = false)
    private Integer depth = 0;

    // ============ Retention Policy ============
    
    /**
     * Number of days before auto-archive, null = unlimited
     */
    private Integer retentionDays;
    
    /**
     * Enable automatic archiving of old documents
     */
    @Column(nullable = false)
    private Boolean autoArchive = false;
    
    /**
     * Date when retention policy was last applied
     */
    private java.time.LocalDateTime lastRetentionCheck;

    // ============ Folder Settings ============
    
    /**
     * Maximum file size allowed in this folder (bytes), null = use org default
     */
    private Long maxFileSize;
    
    /**
     * Allowed MIME types (comma-separated), null = all allowed
     */
    @Column(length = 500)
    private String allowedMimeTypes;
    
    /**
     * Folder description
     */
    @Column(length = 1000)
    private String description;
    
    /**
     * Folder color for UI (hex code)
     */
    private String color;
    
    /**
     * Folder icon name
     */
    private String icon;
    
    /**
     * Sort order among siblings
     */
    private Integer sortOrder = 0;

    // ============ Statistics (cached) ============
    
    /**
     * Total document count in this folder (not including subfolders)
     */
    @Column(nullable = false)
    private Integer documentCount = 0;
    
    /**
     * Total size of documents in this folder (bytes)
     */
    @Column(nullable = false)
    private Long totalSize = 0L;

    // ============ Helper Methods ============

    public void addChild(Resource resource) {
        children.add(resource);
        resource.setParent(this);
        updateFullPath(resource);
    }

    public void removeChild(Resource resource) {
        children.remove(resource);
        resource.setParent(null);
    }
    
    /**
     * Update the full path of a child resource.
     */
    private void updateFullPath(Resource resource) {
        if (resource instanceof Namespace) {
            Namespace ns = (Namespace) resource;
            ns.setFullPath(this.getPath());
            ns.setDepth(this.depth + 1);
        }
    }

    public List<Document> getDocuments() {
        List<Document> docs = new ArrayList<>();
        for (Resource r : children) {
            if (r instanceof Document)
                docs.add((Document) r);
        }
        return docs;
    }

    public List<Namespace> getNamespaces() {
        List<Namespace> nss = new ArrayList<>();
        for (Resource r : children) {
            if (r instanceof Namespace)
                nss.add((Namespace) r);
        }
        return nss;
    }
    
    /**
     * Update materialized path based on parent.
     */
    public void updateMaterializedPath() {
        if (getParent() == null) {
            this.fullPath = "/" + getName();
            this.depth = 0;
        } else {
            this.fullPath = getParent().getPath() + "/" + getName();
            this.depth = ((Namespace) getParent()).getDepth() + 1;
        }
    }
    
    /**
     * Check if MIME type is allowed in this folder.
     */
    public boolean isMimeTypeAllowed(String mimeType) {
        if (allowedMimeTypes == null || allowedMimeTypes.isBlank()) {
            return true;
        }
        String[] allowed = allowedMimeTypes.split(",");
        for (String type : allowed) {
            if (type.trim().equalsIgnoreCase(mimeType)) {
                return true;
            }
            // Support wildcards like "image/*"
            if (type.trim().endsWith("/*")) {
                String prefix = type.trim().replace("/*", "/");
                if (mimeType.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Increment document count and size.
     */
    public void addDocument(long size) {
        this.documentCount = (this.documentCount == null ? 0 : this.documentCount) + 1;
        this.totalSize = (this.totalSize == null ? 0L : this.totalSize) + size;
    }
    
    /**
     * Decrement document count and size.
     */
    public void removeDocument(long size) {
        this.documentCount = Math.max(0, (this.documentCount == null ? 0 : this.documentCount) - 1);
        this.totalSize = Math.max(0L, (this.totalSize == null ? 0L : this.totalSize) - size);
    }
}
