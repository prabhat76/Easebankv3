package com.banking.easbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banking.easbank.entity.Transactions;
import com.banking.easbank.entity.Account;
import com.banking.easbank.repository.TransactionsRepository;
import com.banking.easbank.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing transactions and transfers")
public class TransactionController {

    @Autowired
    private TransactionsRepository transactionsRepository;
    
    @Autowired
    private AccountRepository accountRepository;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfer money between accounts")
    public ResponseEntity<Map<String, Object>> transferMoney(@RequestBody Map<String, Object> transferData) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", 1L);
        response.put("transaction_id", java.util.UUID.randomUUID().toString());
        response.put("from_account_id", transferData.get("fromAccountId"));
        response.put("to_account_id", transferData.get("toAccountId"));
        response.put("amount", new BigDecimal(transferData.get("amount").toString()));
        response.put("type", "TRANSFER");
        response.put("status", "SUCCESS");
        response.put("created_at", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transaction history", description = "Retrieve transaction history for an account")
    public ResponseEntity<Map<String, Object>> getTransactionHistory(
            @Parameter(description = "Account ID") @PathVariable String accountId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> transaction1 = new HashMap<>();
        transaction1.put("id", 1L);
        transaction1.put("transaction_id", "TXN001");
        transaction1.put("type", "CREDIT");
        transaction1.put("amount", new BigDecimal("1000.00"));
        transaction1.put("status", "COMPLETED");
        transaction1.put("created_at", LocalDateTime.now().minusDays(1));

        Map<String, Object> transaction2 = new HashMap<>();
        transaction2.put("id", 2L);
        transaction2.put("transaction_id", "TXN002");
        transaction2.put("type", "DEBIT");
        transaction2.put("amount", new BigDecimal("50.00"));
        transaction2.put("status", "COMPLETED");
        transaction2.put("created_at", LocalDateTime.now().minusHours(2));

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
        response.put("id", 1L);
        response.put("transaction_id", transactionId);
        response.put("type", "TRANSFER");
        response.put("amount", new BigDecimal("500.00"));
        response.put("from_account_id", 12345L);
        response.put("to_account_id", 67890L);
        response.put("status", "COMPLETED");
        response.put("created_at", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/recalculate-balance/{accountId}")
    @Operation(summary = "Recalculate account balance", description = "Recalculate balance based on all transactions")
    public ResponseEntity<Map<String, Object>> recalculateBalance(@PathVariable Long accountId) {
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (!accountOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Account not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Account account = accountOpt.get();
            List<Transactions> transactions = transactionsRepository.findByAccountId(accountId);
            
            BigDecimal calculatedBalance = BigDecimal.ZERO;
            for (Transactions txn : transactions) {
                if ("DEPOSIT".equals(txn.getType()) || "CREDIT".equals(txn.getType())) {
                    calculatedBalance = calculatedBalance.add(txn.getAmount());
                } else if ("WITHDRAWAL".equals(txn.getType()) || "DEBIT".equals(txn.getType())) {
                    calculatedBalance = calculatedBalance.subtract(txn.getAmount());
                }
            }
            
            account.setBalance(calculatedBalance);
            accountRepository.save(account);
            
            Map<String, Object> response = new HashMap<>();
            response.put("accountId", accountId);
            response.put("oldBalance", account.getBalance());
            response.put("newBalance", calculatedBalance);
            response.put("transactionCount", transactions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to recalculate balance: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money", description = "Deposit money into an account")
    public ResponseEntity<Map<String, Object>> depositMoney(@RequestBody Map<String, Object> depositData) {
        try {
            if (depositData.get("from_account_id") == null || depositData.get("amount") == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing required fields: from_account_id and amount");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Long accountId = Long.parseLong(depositData.get("from_account_id").toString());
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            
            if (!accountOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Account not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Account account = accountOpt.get();
            
            // Update account balance
            BigDecimal depositAmount = new BigDecimal(depositData.get("amount").toString());
            account.setBalance(account.getBalance().add(depositAmount));
            accountRepository.save(account);
            
            // Create and save transaction
            Transactions transaction = new Transactions();
            transaction.setTransactionId(java.util.UUID.randomUUID().toString());
            transaction.setFromAccount(account);
            transaction.setToAccount(account);
            transaction.setAmount(depositAmount);
            transaction.setType("DEPOSIT");
            transaction.setStatus("SUCCESS");
            transaction.setCreatedAt(LocalDateTime.now());
            
            Transactions savedTransaction = transactionsRepository.save(transaction);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedTransaction.getId());
            response.put("transaction_id", savedTransaction.getTransactionId());
            response.put("from_account_id", savedTransaction.getFromAccount().getId());
            response.put("to_account_id", savedTransaction.getToAccount().getId());
            response.put("amount", savedTransaction.getAmount());
            response.put("type", savedTransaction.getType());
            response.put("status", savedTransaction.getStatus());
            response.put("created_at", savedTransaction.getCreatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process deposit: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraw money from an account")
    public ResponseEntity<Map<String, Object>> withdrawMoney(@RequestBody Map<String, Object> withdrawData) {
        try {
            if (withdrawData.get("from_account_id") == null || withdrawData.get("amount") == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing required fields: from_account_id and amount");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Long accountId = Long.parseLong(withdrawData.get("from_account_id").toString());
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            
            if (!accountOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Account not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Account account = accountOpt.get();
            BigDecimal withdrawAmount = new BigDecimal(withdrawData.get("amount").toString());
            
            // Check sufficient balance
            if (account.getBalance().compareTo(withdrawAmount) < 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Insufficient balance");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Update account balance
            account.setBalance(account.getBalance().subtract(withdrawAmount));
            accountRepository.save(account);
            
            // Create and save transaction
            Transactions transaction = new Transactions();
            transaction.setTransactionId(java.util.UUID.randomUUID().toString());
            transaction.setFromAccount(account);
            transaction.setToAccount(account);
            transaction.setAmount(withdrawAmount);
            transaction.setType("WITHDRAWAL");
            transaction.setStatus("SUCCESS");
            transaction.setCreatedAt(LocalDateTime.now());
            
            Transactions savedTransaction = transactionsRepository.save(transaction);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedTransaction.getId());
            response.put("transaction_id", savedTransaction.getTransactionId());
            response.put("from_account_id", savedTransaction.getFromAccount().getId());
            response.put("to_account_id", savedTransaction.getToAccount().getId());
            response.put("amount", savedTransaction.getAmount());
            response.put("type", savedTransaction.getType());
            response.put("status", savedTransaction.getStatus());
            response.put("created_at", savedTransaction.getCreatedAt());
            response.put("remaining_balance", account.getBalance());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process withdrawal: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

}