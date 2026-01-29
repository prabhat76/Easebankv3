package com.banking.easbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "System", description = "System health and information APIs")
public class HomeController {

    @GetMapping("/")
    @Operation(summary = "Welcome message", description = "Get welcome message and API information")
    public String home() {
        return "Welcome to EasBank! Go to /swagger-ui/index.html for API docs";
    }
    
    @GetMapping("/api/health")
    @Operation(summary = "Health check", description = "Check if the API is running")
    public String health() {
        return "EasBank API is running!";
    }
}