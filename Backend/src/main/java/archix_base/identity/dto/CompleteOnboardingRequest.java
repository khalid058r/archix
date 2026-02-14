package archix_base.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompleteOnboardingRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String organizationName;

    private String organizationDescription;

    // Optional: user can update their info during onboarding
    private String firstName;
    private String lastName;
    private String phone;
}
