package com.banking.easbank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferResponse {
    private String message;
    private String transactionIdDebit;
    private String transactionIdCredit;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private BigDecimal newFromAccountBalance;
    private BigDecimal newToAccountBalance;
    private LocalDateTime timestamp;

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionIdDebit() {
        return transactionIdDebit;
    }

    public void setTransactionIdDebit(String transactionIdDebit) {
        this.transactionIdDebit = transactionIdDebit;
    }

    public String getTransactionIdCredit() {
        return transactionIdCredit;
    }

    public void setTransactionIdCredit(String transactionIdCredit) {
        this.transactionIdCredit = transactionIdCredit;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getNewFromAccountBalance() {
        return newFromAccountBalance;
    }

    public void setNewFromAccountBalance(BigDecimal newFromAccountBalance) {
        this.newFromAccountBalance = newFromAccountBalance;
    }

    public BigDecimal getNewToAccountBalance() {
        return newToAccountBalance;
    }

    public void setNewToAccountBalance(BigDecimal newToAccountBalance) {
        this.newToAccountBalance = newToAccountBalance;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}