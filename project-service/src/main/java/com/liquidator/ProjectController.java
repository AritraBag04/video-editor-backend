package com.liquidator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/project")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;
    @PostMapping()
    public ResponseEntity<?> createProject(@RequestBody Project project) {
        log.info("Creating project with request ID: {}", project);
        projectRepository.save(project);
        return ResponseEntity.ok("Project created successfully with ID: " + project.getRequestId());
    }
    @GetMapping()
    public ResponseEntity<String> getDownloadLink(@RequestParam String requestId) {
        return projectRepository.findById(requestId)
                .map(project -> ResponseEntity.ok(project.getDownloadLink()))
                .orElse(ResponseEntity.notFound().build());
    }
}
