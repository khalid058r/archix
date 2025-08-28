package archix_base.controllers;

import archix_base.dto.OrganizationDto;
import archix_base.services.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<List<OrganizationDto>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDto> getOrganizationById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    @PostMapping
    public ResponseEntity<OrganizationDto> createOrganization(@RequestBody OrganizationDto organizationDto) {
        return ResponseEntity.ok(organizationService.createOrganization(organizationDto));
    }

    @PutMapping
    public ResponseEntity<OrganizationDto> updateOrganization(@RequestBody OrganizationDto organizationDto) {
        return ResponseEntity.ok(organizationService.updateOrganization(organizationDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganizationById(id);
        return ResponseEntity.ok().build();
    }
}
