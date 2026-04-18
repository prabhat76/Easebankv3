package com.banking.easbank.repository;

import com.banking.easbank.entity.Transactions;
import com.banking.easbank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Long> {
        // Find transactions where fromAccountId or toAccountId matches, paginated
        Page<Transactions> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId, Pageable pageable);
    
    // Find by transaction ID
    Optional<Transactions> findByTransactionId(String transactionId);
    
    // Find all transactions for a specific account (either from or to)
    @Query("SELECT t FROM Transactions t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.createdAt DESC")
    List<Transactions> findByAccount(@Param("account") Account account);
    
    // Find transactions by account ID
    @Query("SELECT t FROM Transactions t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.createdAt DESC")
    List<Transactions> findByAccountId(@Param("accountId") Long accountId);
    
    // Find by status
    List<Transactions> findByStatus(String status);
    
    // Find by type
    List<Transactions> findByType(String type);
    
    // Find transactions within date range
    List<Transactions> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find recent transactions (last 10)
    @Query("SELECT t FROM Transactions t ORDER BY t.createdAt DESC LIMIT 10")
    List<Transactions> findRecentTransactions();
}
