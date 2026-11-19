package edu.academic.solutions;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ActivityController {
    private final AcademicSolutionsService service;

    public ActivityController(AcademicSolutionsService service) {
        this.service = service;
    }

    @GetMapping("/modules")
    public List<ModuleDefinition> modules() {
        return service.getModules();
    }

    @GetMapping("/records")
    public List<ActivityRecord> records(@RequestParam(required = false) String module) {
        return service.getRecords(module);
    }

    @PostMapping("/records")
    public ActivityRecord create(@Valid @RequestBody ActivityRequest request) {
        return service.create(request);
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return service.getDashboardStats();
    }
}
