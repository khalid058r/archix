package archix_base.controllers;

import archix_base.dto.ChangeOrganizationDto;
import archix_base.dto.CreateDepartmentDto;
import archix_base.dto.DepartmentDto;
import archix_base.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping
    public ResponseEntity<DepartmentDto> createDepartment(@RequestBody CreateDepartmentDto createDepartmentDto) {
        return ResponseEntity.ok(departmentService.createDepartment(createDepartmentDto));
    }

    @PutMapping
    public ResponseEntity<DepartmentDto> updateDepartment(@RequestBody DepartmentDto departmentDto) {
        return ResponseEntity.ok(departmentService.updateDepartment(departmentDto));
    }

    @PutMapping("/change-org")
    public ResponseEntity<DepartmentDto> changeOrganization(@RequestBody ChangeOrganizationDto changeOrganizationDto) {
        return ResponseEntity.ok(departmentService.changeOrganization(changeOrganizationDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartmentById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<DepartmentDto>> getDepartmentsByOrganization(@PathVariable Long organizationId) {
        return ResponseEntity.ok(departmentService.getDepartmentsByOrganization(organizationId));
    }
}
