package archix_base.organization.entity;

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
    @OneToMany(mappedBy = "organization")
    private List<Department> departments;
}








