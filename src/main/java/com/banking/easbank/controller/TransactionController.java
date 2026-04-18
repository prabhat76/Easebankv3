package com.banking.easbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.banking.easbank.entity.Transactions;
import com.banking.easbank.entity.Account;
import com.banking.easbank.repository.TransactionsRepository;
import com.banking.easbank.repository.AccountRepository;
import com.banking.easbank.dto.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing transactions and transfers")
public class TransactionController {

    @Autowired
    private TransactionsRepository transactionsRepository;
    
    @Autowired
    private AccountRepository accountRepository;

    // Helper method to map Transactions entity to TransactionResponse DTO
    private TransactionResponse mapToTransactionResponse(Transactions transaction) {
        TransactionResponse dto = new TransactionResponse();
        dto.setId(transaction.getId());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setFromAccountId(transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null);
        dto.setToAccountId(transaction.getToAccount() != null ? transaction.getToAccount().getId() : null);
        dto.setStatus(transaction.getStatus());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfer money between accounts")
    @Transactional // Ensure atomicity for multiple database operations
    public ResponseEntity<TransferResponse> transferMoney(@Valid @RequestBody TransferRequest transferRequest) {
        TransferResponse response = new TransferResponse();
        try {
            Long fromAccountId = transferRequest.getFromAccountId();
            Long toAccountId = transferRequest.getToAccountId();
            BigDecimal amount = transferRequest.getAmount();

            if (fromAccountId.equals(toAccountId)) {
                response.setMessage("Cannot transfer money to the same account.");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Account> fromAccountOpt = accountRepository.findById(fromAccountId);
            Optional<Account> toAccountOpt = accountRepository.findById(toAccountId);

            if (!fromAccountOpt.isPresent()) {
                response.setMessage("Source account not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (!toAccountOpt.isPresent()) {
                response.setMessage("Destination account not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Account fromAccount = fromAccountOpt.get();
            Account toAccount = toAccountOpt.get();

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                response.setMessage("Insufficient balance in source account.");
                return ResponseEntity.badRequest().body(response);
            }

            // Deduct from source account
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            accountRepository.save(fromAccount);

            // Add to destination account
            toAccount.setBalance(toAccount.getBalance().add(amount));
            accountRepository.save(toAccount);

            // Create debit transaction
            Transactions debitTransaction = new Transactions();
            debitTransaction.setTransactionId(UUID.randomUUID().toString());
            debitTransaction.setFromAccount(fromAccount);
            debitTransaction.setToAccount(toAccount); // To account is the recipient
            debitTransaction.setAmount(amount);
            debitTransaction.setType("TRANSFER_DEBIT");
            debitTransaction.setStatus("COMPLETED");
            debitTransaction.setCreatedAt(LocalDateTime.now());
            transactionsRepository.save(debitTransaction);

            // Create credit transaction
            Transactions creditTransaction = new Transactions();
            creditTransaction.setTransactionId(UUID.randomUUID().toString());
            creditTransaction.setFromAccount(fromAccount); // From account is the sender
            creditTransaction.setToAccount(toAccount);
            creditTransaction.setAmount(amount);
            creditTransaction.setType("TRANSFER_CREDIT");
            creditTransaction.setStatus("COMPLETED");
            creditTransaction.setCreatedAt(LocalDateTime.now());
            transactionsRepository.save(creditTransaction);

            response.setMessage("Transfer successful");
            response.setTransactionIdDebit(debitTransaction.getTransactionId());
            response.setTransactionIdCredit(creditTransaction.getTransactionId());
            response.setFromAccountId(fromAccountId);
            response.setToAccountId(toAccountId);
            response.setAmount(amount);
            response.setNewFromAccountBalance(fromAccount.getBalance());
            response.setNewToAccountBalance(toAccount.getBalance());
            response.setTimestamp(LocalDateTime.now());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setMessage("Failed to process transfer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transaction history", description = "Retrieve transaction history for an account")
    public ResponseEntity<AccountTransactionHistoryResponse> getTransactionHistory(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        AccountTransactionHistoryResponse response = new AccountTransactionHistoryResponse();
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (!accountOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Transactions> transactionsPage = transactionsRepository.findByFromAccountIdOrToAccountId(accountId, accountId, pageable);
            List<Transactions> pagedList = transactionsPage.getContent();
            // ...existing code to map pagedList to response...
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction details", description = "Get details of a specific transaction")
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId) {
        try {
            Optional<Transactions> transactionOpt = transactionsRepository.findByTransactionId(transactionId);

            if (!transactionOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            TransactionResponse response = mapToTransactionResponse(transactionOpt.get());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/recalculate-balance/{accountId}")
    @Operation(summary = "Recalculate account balance", description = "Recalculate balance based on all transactions")
    @Transactional // Ensure atomicity
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
    @Transactional // Ensure atomicity
    public ResponseEntity<Map<String, Object>> depositMoney(@Valid @RequestBody DepositRequest depositRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long accountId = depositRequest.getAccountId();
            BigDecimal depositAmount = depositRequest.getAmount();

            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (!accountOpt.isPresent()) {
                response.put("error", "Account not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Account account = accountOpt.get();
            // Update account balance
            account.setBalance(account.getBalance().add(depositAmount));
            accountRepository.save(account);

            // Create and save transaction
            Transactions transaction = new Transactions();
            transaction.setTransactionId(java.util.UUID.randomUUID().toString());
            transaction.setFromAccount(account); // For deposit, from and to can be the same account or null for external source
            transaction.setToAccount(account);
            transaction.setAmount(depositAmount);
            transaction.setType("DEPOSIT");
            transaction.setStatus("SUCCESS");
            transaction.setCreatedAt(LocalDateTime.now());

            Transactions savedTransaction = transactionsRepository.save(transaction);

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
            response.put("error", "Failed to process deposit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraw money from an account")
    @Transactional // Ensure atomicity
    public ResponseEntity<Map<String, Object>> withdrawMoney(@Valid @RequestBody WithdrawRequest withdrawRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long accountId = withdrawRequest.getAccountId();
            BigDecimal withdrawAmount = withdrawRequest.getAmount();

            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (!accountOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Account not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Account account = accountOpt.get();
            // Check sufficient balance
            if (account.getBalance().compareTo(withdrawAmount) < 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Insufficient balance");
                errorResponse.put("currentBalance", account.getBalance());
                errorResponse.put("withdrawalAmount", withdrawAmount);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Update account balance
            account.setBalance(account.getBalance().subtract(withdrawAmount));
            accountRepository.save(account);

            // Create and save transaction
            Transactions transaction = new Transactions();
            transaction.setTransactionId(java.util.UUID.randomUUID().toString());
            transaction.setFromAccount(account);
            transaction.setToAccount(account); // For withdrawal, from and to can be the same account or null for external destination
            transaction.setAmount(withdrawAmount);
            transaction.setType("WITHDRAWAL");
            transaction.setStatus("SUCCESS");
            transaction.setCreatedAt(LocalDateTime.now());

            Transactions savedTransaction = transactionsRepository.save(transaction);

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}