package archix_base.organization.entity;

import archix_base.identity.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "department")
    private List<User> users;

    @ManyToOne
    private Organization organization;

    // Hierarchy: Parent Department
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Department parentDepartment;

    // Hierarchy: Child Departments
    @OneToMany(mappedBy = "parentDepartment", cascade = CascadeType.ALL)
    private List<Department> children;

    // Manager of this department
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    // Storage quota in bytes (null = unlimited)
    private Long storageQuotaBytes;
    private Long storageUsedBytes;
}
