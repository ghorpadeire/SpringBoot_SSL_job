package com.sslassessment.controller;

import com.sslassessment.model.SslAssessment;
import com.sslassessment.service.AssessmentStorageService;
import com.sslassessment.service.SslAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AssessmentController {

    @Autowired
    private SslAssessmentService sslAssessmentService;

    @Autowired
    private AssessmentStorageService storageService;

    @PostMapping("/assess")
    public ResponseEntity<?> assessHostname(@RequestBody Map<String, String> request) {
        String hostname = request.get("hostname");

        if (hostname == null || hostname.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Hostname is required"));
        }

        hostname = hostname.trim().toLowerCase();

        if (!sslAssessmentService.isValidHostname(hostname)) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid hostname. IP addresses and ports are not allowed."));
        }

        // Perform assessment
        SslAssessment assessment = sslAssessmentService.assessHost(hostname);

        // Save to storage
        storageService.saveAssessment(assessment);

        return ResponseEntity.ok(assessment);
    }

    @GetMapping("/assessments")
    public ResponseEntity<List<SslAssessment>> getRecentAssessments() {
        List<SslAssessment> assessments = storageService.getRecentAssessments();
        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/assessment/{hostname}")
    public ResponseEntity<?> getAssessment(@PathVariable String hostname) {
        SslAssessment assessment = storageService.getAssessment(hostname);

        if (assessment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Assessment not found for hostname: " + hostname));
        }

        return ResponseEntity.ok(assessment);
    }

    @DeleteMapping("/assessment/{hostname}")
    public ResponseEntity<?> deleteAssessment(@PathVariable String hostname) {
        boolean deleted = storageService.deleteAssessment(hostname);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Assessment not found for hostname: " + hostname));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Assessment deleted successfully");

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
