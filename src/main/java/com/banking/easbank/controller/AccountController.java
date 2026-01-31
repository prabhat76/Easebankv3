package com.banking.easbank.controller;
import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.banking.easbank.entity.Account;
import com.banking.easbank.entity.User;
import com.banking.easbank.service.AccountService;
import com.banking.easbank.service.UserService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {
    
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService AccountService;
    @GetMapping("/{accountId}")
    @Operation(summary = "Get account details", description = "Retrieve account information by account ID")
    public ResponseEntity<Map<String, Object>> getAccount(
            @Parameter(description = "Account ID") @PathVariable String accountId) {
        Map<String, Object> account = new HashMap<>();
        account.put("accountId", accountId);
        account.put("accountNumber", "ACC" + accountId);
        account.put("balance", new BigDecimal("5000.00"));
        account.put("accountType", "SAVINGS");
        account.put("status", "ACTIVE");
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Check account balance", description = "Get current balance of the account")
    public ResponseEntity<Map<String, Object>> getBalance(
            @Parameter(description = "Account ID") @PathVariable String accountId) {
        Map<String, Object> response = new HashMap<>();
        response.put("accountId", accountId);
        response.put("balance", new BigDecimal("5000.00"));
        response.put("currency", "USD");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/AccountOpening")
    @Operation(summary = "Open account for existing user", description = "Open account using email or userId")
    public ResponseEntity<Map<String, Object>> openAccount(@RequestBody Map<String, Object> request) {
        try {
            Long userId = null;
            
            // Check if userId is provided
            if (request.containsKey("userId")) {
                userId = Long.valueOf(request.get("userId").toString());
            } 
            // Check if email is provided instead
            else if (request.containsKey("email")) {
                String email = request.get("email").toString();
                Optional<User> user = userService.findByEmail(email);
                if (user.isPresent()) {
                    userId = user.get().getId();
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "User not found");
                    error.put("message", "No user found with email: " + email);
                    return ResponseEntity.status(404).body(error);
                }
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Missing parameter");
                error.put("message", "Either userId or email is required");
                return ResponseEntity.status(400).body(error);
            }
            
            String accountType = request.get("accountType").toString();
            BigDecimal initialDeposit = new BigDecimal(request.get("initialDeposit").toString());
            
            Account account = AccountService.openAccount(userId, accountType, initialDeposit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account opened successfully");
            response.put("accountId", account.getId());
            response.put("accountNumber", account.getAccountNumber());
            response.put("accountType", account.getAccountType());
            response.put("balance", account.getBalance());
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Account opening failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }
}