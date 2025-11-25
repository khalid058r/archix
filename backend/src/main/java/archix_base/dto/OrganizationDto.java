package archix_base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrganizationDto {
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
}
