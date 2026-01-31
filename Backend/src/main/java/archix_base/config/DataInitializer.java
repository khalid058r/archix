package archix_base.config;

import archix_base.identity.entity.User;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import archix_base.organization.repo.DepartmentRepo;
import archix_base.organization.repo.OrganizationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import archix_base.document.repo.DocumentRepo;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final OrganizationRepo organizationRepo;
    private final DepartmentRepo departmentRepo;
    private final DocumentRepo documentRepo;
    private final archix_base.identity.repo.RoleRepo roleRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createDefaultRoles();
        createSuperAdmin();
        fixOrphanUsers();
        fixOrphanDocuments();
    }

    private void createDefaultRoles() {
        for (archix_base.identity.entity.RoleType type : archix_base.identity.entity.RoleType.values()) {
            if (!roleRepo.existsByType(type)) {
                archix_base.identity.entity.Role role = new archix_base.identity.entity.Role(type);
                role.setDescription("Default role for " + type.name());
                roleRepo.save(role);
            }
        }
    }

    private void createSuperAdmin() {
        if (userRepo.existsByEmail("admin@archix.com")) {
            return;
        }

        System.out.println("Creating Super Admin user...");

        // 1. Create Organization
        Organization org = new Organization();
        org.setName("Archix Administration");
        org.setCreatedAt(LocalDateTime.now());
        org.setDescription("Main administration organization");
        org = organizationRepo.save(org);

        // 2. Create Dept
        Department dept = new Department();
        dept.setName("IT Security");
        dept.setOrganization(org);
        dept.setCreatedAt(LocalDateTime.now());
        dept = departmentRepo.save(dept);

        // 3. Create User
        User admin = new User();
        // admin.setUsername("admin"); // Removed: Username is email
        admin.setEmail("admin@archix.com");
        admin.setFirstName("Super");
        admin.setLastName("Admin");
        admin.setPasswordHash(passwordEncoder.encode("password123"));
        admin.setDepartment(dept);
        // admin.setJobTitle("System Administrator"); // Removed: Field does not exist
        admin.setCreatedAt(LocalDateTime.now());
        // admin.setUpdatedAt(LocalDateTime.now()); // Removed: Field does not exist
        admin.setIsActive(true);

        // Assign Roles
        archix_base.identity.entity.Role superAdminRole = roleRepo
                .findByType(archix_base.identity.entity.RoleType.SUPER_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        archix_base.identity.entity.Role adminRole = roleRepo.findByType(archix_base.identity.entity.RoleType.ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        admin.setRoles(java.util.Set.of(superAdminRole, adminRole));

        userRepo.save(admin);
        System.out.println("Super Admin created: admin@archix.com / password123");
    }

    private void fixOrphanUsers() {
        List<User> orphans = userRepo.findAll().stream()
                .filter(u -> u.getDepartment() == null)
                .toList();

        if (orphans.isEmpty()) {
            return;
        }

        System.out.println("Found " + orphans.size() + " orphan users. Creating personal organizations...");

        for (User user : orphans) {
            createPersonalOrganization(user);
        }
    }

    private void fixOrphanDocuments() {
        List<archix_base.document.entity.Document> orphans = documentRepo.findAll().stream()
                .filter(d -> d.getOrganization() == null)
                .toList();

        if (orphans.isEmpty()) {
            return;
        }

        System.out.println("Found " + orphans.size() + " orphan documents. Fixing...");

        for (archix_base.document.entity.Document doc : orphans) {
            if (doc.getCreatedBy() != null && doc.getCreatedBy().getDepartment() != null
                    && doc.getCreatedBy().getDepartment().getOrganization() != null) {
                doc.setOrganization(doc.getCreatedBy().getDepartment().getOrganization());
                documentRepo.save(doc);
                System.out.println("Fixed document: " + doc.getFileName());
            }
        }
    }

    private void createPersonalOrganization(User user) {
        String orgName = (user.getFirstName() != null ? user.getFirstName() : user.getUsername()) + "'s Organization";

        // 1. Create Organization
        Organization org = new Organization();
        org.setName(orgName);
        org.setCreatedAt(LocalDateTime.now());
        org.setDescription("Personal organization for " + user.getEmail());
        org = organizationRepo.save(org);

        // 2. Create Default Department
        Department dept = new Department();
        dept.setName("Main");
        dept.setDescription("Default department");
        dept.setCreatedAt(LocalDateTime.now());
        dept.setOrganization(org);
        dept = departmentRepo.save(dept);

        // 3. Link User
        user.setDepartment(dept);
        userRepo.save(user);

        System.out.println("Created Personal Organization for user: " + user.getEmail());
    }
}
