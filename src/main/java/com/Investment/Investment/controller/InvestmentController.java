package com.Investment.Investment.controller;

import com.Investment.Investment.dto.InvestmentRequest;
import com.Investment.Investment.dto.InvestmentResponse;
import com.Investment.Investment.dto.PaginatedResponse;
import com.Investment.Investment.service.FirebaseService;
import com.Investment.Investment.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/investments")
@CrossOrigin(origins = "*")
public class InvestmentController {

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createInvestment(@Valid @RequestBody InvestmentRequest request) {
        try {
            if ("Other".equalsIgnoreCase(request.getProfession()) && 
                (request.getProfessionOther() == null || request.getProfessionOther().trim().isEmpty())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "professionOther is required when profession is 'Other'");
                errorResponse.put("message", "If profession is 'Other', you must provide a value for professionOther");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getCurrentInvestments() != null && 
                request.getCurrentInvestments().stream().anyMatch(inv -> "Other".equalsIgnoreCase(inv)) &&
                (request.getCurrentInvestmentsOther() == null || request.getCurrentInvestmentsOther().trim().isEmpty())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "currentInvestmentsOther is required when currentInvestments contains 'Other'");
                errorResponse.put("message", "If currentInvestments contains 'Other', you must provide a value for currentInvestmentsOther");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String id = firebaseService.saveInvestment(request);
            
            // Send email notification to the user
            try {
                emailService.sendReservationEmail(request.getEmailAddress(),request.getFirstName());
            } catch (Exception e) {
                // Log error but don't fail the request if email fails
                System.err.println("Email sending failed: " + e.getMessage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Investment data saved successfully");
            response.put("id", id);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to save investment data");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllInvestments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        try {
            // Validate pagination parameters
            if (page < 0) {
                page = 0;
            }
            if (size < 1) {
                size = 10;
            }
            if (size > 100) {
                size = 100; // Max page size
            }

            PaginatedResponse<InvestmentResponse> paginatedResponse = firebaseService.getAllInvestmentsPaginated(page, size, name);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", paginatedResponse.getData());
            response.put("pagination", Map.of(
                "page", paginatedResponse.getPage(),
                "size", paginatedResponse.getSize(),
                "totalElements", paginatedResponse.getTotalElements(),
                "totalPages", paginatedResponse.getTotalPages(),
                "hasNext", paginatedResponse.isHasNext(),
                "hasPrevious", paginatedResponse.isHasPrevious()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch investments");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getInvestmentById(@PathVariable String id) {
        try {
            InvestmentResponse investment = firebaseService.getInvestmentById(id);
            
            if (investment == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Investment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", investment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch investment");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteInvestment(@PathVariable String id) {
        try {
            boolean deleted = firebaseService.deleteInvestment(id);
            
            if (!deleted) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Investment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Investment deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete investment");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

