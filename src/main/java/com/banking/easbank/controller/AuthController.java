package com.banking.easbank.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.easbank.dto.LoginRequest;
import com.banking.easbank.entity.LoginHistory;
import com.banking.easbank.entity.User;
import com.banking.easbank.repository.LoginHistoryRepository;
import com.banking.easbank.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

     @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> user = userService.findByEmail(loginRequest.getEmail());
        LocalDateTime loginTime = LocalDateTime.now();
        
        if (user.isPresent() && user.get().getPassword().equals(loginRequest.getPassword())) {
            // Save successful login
            loginHistoryRepository.save(new LoginHistory(loginRequest.getEmail(), loginTime, true));
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("userId", user.get().getId());
            response.put("email", user.get().getEmail());
            response.put("firstName", user.get().getFirstName());
            response.put("logintime", loginTime);
            return ResponseEntity.ok(response);
        } else {
            // Save failed login attempt
            loginHistoryRepository.save(new LoginHistory(loginRequest.getEmail(), loginTime, false));
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Invalid credentials");
            response.put("message", "Email or password is incorrect");
            return ResponseEntity.status(401).body(response);
        }
    }
}
