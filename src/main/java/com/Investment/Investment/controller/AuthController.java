package com.Investment.Investment.controller;

import com.Investment.Investment.dto.AuthRequest;
import com.Investment.Investment.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    // Fixed credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin@123";

    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> generateToken(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Validate credentials
            if (!ADMIN_USERNAME.equals(authRequest.getUsername()) || 
                !ADMIN_PASSWORD.equals(authRequest.getPassword())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Invalid credentials");
                errorResponse.put("message", "Username or password is incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Generate token for authenticated user
            String token = jwtUtil.generateToken(authRequest.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("message", "Authentication successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate token");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

