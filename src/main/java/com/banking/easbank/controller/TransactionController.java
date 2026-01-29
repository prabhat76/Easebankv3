package com.banking.easbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing transactions and transfers")
public class TransactionController {

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfer money between accounts")
    public ResponseEntity<Map<String, Object>> transferMoney(@RequestBody Map<String, Object> transferData) {
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", "TXN123456");
        response.put("fromAccount", transferData.get("fromAccount"));
        response.put("toAccount", transferData.get("toAccount"));
        response.put("amount", transferData.get("amount"));
        response.put("status", "SUCCESS");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transaction history", description = "Retrieve transaction history for an account")
    public ResponseEntity<Map<String, Object>> getTransactionHistory(
            @Parameter(description = "Account ID") @PathVariable String accountId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> transaction1 = new HashMap<>();
        transaction1.put("transactionId", "TXN001");
        transaction1.put("type", "CREDIT");
        transaction1.put("amount", new BigDecimal("1000.00"));
        transaction1.put("description", "Salary deposit");
        transaction1.put("timestamp", LocalDateTime.now().minusDays(1));

        Map<String, Object> transaction2 = new HashMap<>();
        transaction2.put("transactionId", "TXN002");
        transaction2.put("type", "DEBIT");
        transaction2.put("amount", new BigDecimal("50.00"));
        transaction2.put("description", "ATM withdrawal");
        transaction2.put("timestamp", LocalDateTime.now().minusHours(2));

        Map<String, Object> response = new HashMap<>();
        response.put("accountId", accountId);
        response.put("transactions", List.of(transaction1, transaction2));
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", 2);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction details", description = "Get details of a specific transaction")
    public ResponseEntity<Map<String, Object>> getTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId) {
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", transactionId);
        response.put("type", "TRANSFER");
        response.put("amount", new BigDecimal("500.00"));
        response.put("fromAccount", "ACC12345");
        response.put("toAccount", "ACC67890");
        response.put("status", "COMPLETED");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}