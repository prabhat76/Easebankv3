package com.banking.easbank.controller;

import com.banking.easbank.entity.User;
import com.banking.easbank.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing bank customers")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new bank customer")
    public ResponseEntity<User> registerUser(@Valid @RequestBody  User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile", description = "Retrieve user profile information")
    public ResponseEntity<User> getUserProfile(
            @Parameter(description = "User ID") @PathVariable  Long userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user profile", description = "Update user profile information")
    public ResponseEntity<User> updateUserProfile(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody User userData) {
        Optional<User> existingUser = userService.getUserById(userId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (userData.getFirstName() != null) user.setFirstName(userData.getFirstName());
            if (userData.getLastName() != null) user.setLastName(userData.getLastName());
            if (userData.getEmail() != null) user.setEmail(userData.getEmail());
            if (userData.getPhone() != null) user.setPhone(userData.getPhone());
            User updatedUser = userService.saveUser(user);
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete a user")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}