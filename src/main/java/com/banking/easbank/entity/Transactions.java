package com.banking.easbank.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transactions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String transactionId;
    private BigDecimal amount;
    private String type; // TRANSFER, DEPOSIT, WITHDRAWAL
    private String status; // PENDING, COMPLETED, FAILED
    
    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;
    
    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;
    
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Transactions() {
        // Default constructor
    }

    public Transactions(Long id, String transactionId, BigDecimal amount, String type, String status,
            Account fromAccount, Account toAccount, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.createdAt = createdAt;
    }
    
    // Constructors, getters, setters...
}
