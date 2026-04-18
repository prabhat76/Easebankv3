package com.banking.easbank.controller;

// ...existing code...
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.banking.easbank.entity.Account;
import com.banking.easbank.entity.User;
import com.banking.easbank.service.AccountService;
import com.banking.easbank.service.UserService;
import com.banking.easbank.dto.AccountOpeningRequest;
import com.banking.easbank.dto.AccountDetailsResponse;
import com.banking.easbank.dto.AccountBalanceResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {
    
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @GetMapping("/{accountId}")
    @Operation(summary = "Get account details", description = "Retrieve account information by account ID")
    public ResponseEntity<AccountDetailsResponse> getAccount(
            @Parameter(description = "Account ID") @PathVariable Long accountId) {
       try {

        Optional<Account> accountOpt = accountService.getAccountById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            AccountDetailsResponse accountDetails = new AccountDetailsResponse();
            accountDetails.setAccountId(account.getId());
            accountDetails.setAccountNumber(account.getAccountNumber());
            accountDetails.setAccountType(account.getAccountType());
            accountDetails.setBalance(account.getBalance());
            accountDetails.setUserId(account.getUser().getId());
            return ResponseEntity.ok(accountDetails);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
       } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
       } 
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Check account balance", description = "Get current balance of the account")
    public ResponseEntity<AccountBalanceResponse> getBalance(
            @Parameter(description = "Account ID") @PathVariable Long accountId) {
        AccountBalanceResponse response = new AccountBalanceResponse();
        try {
            Optional<Account> accountOpt = accountService.getAccountById(accountId);
            if (!accountOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            Account account = accountOpt.get();
            response.setAccountId(accountId);
            response.setBalance(account.getBalance());
            response.setCurrency("USD"); // Assuming USD as default currency
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/AccountOpening")
    @Operation(summary = "Open account for existing user", description = "Open account using email or userId")
    public ResponseEntity<Map<String, Object>> openAccount(@Valid @RequestBody AccountOpeningRequest request) {
        try {
            Long userId = null;
            
            // Check if userId is provided
            if (request.getUserId() != null) {
                userId = request.getUserId();
            } 
            // Check if email is provided instead
            else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                String email = request.getEmail();
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
            
            String accountType = request.getAccountType();
            BigDecimal initialDeposit = request.getInitialDeposit();
            
            Account account = accountService.openAccount(userId, accountType, initialDeposit);
            
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}