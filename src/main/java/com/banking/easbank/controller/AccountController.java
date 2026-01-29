package com.banking.easbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {

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

    @PostMapping
    @Operation(summary = "Create new account", description = "Create a new bank account")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Map<String, Object> accountData) {
        Map<String, Object> response = new HashMap<>();
        response.put("accountId", "12345");
        response.put("accountNumber", "ACC12345");
        response.put("message", "Account created successfully");
        response.put("status", "ACTIVE");
        return ResponseEntity.ok(response);
    }
}