package com.banking.easbank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public class AccountOpeningRequest {

    private Long userId; // Optional, if email is provided
    private String email; // Optional, if userId is provided

    @NotBlank(message = "Account type cannot be blank")
    @Pattern(regexp = "SAVINGS|CHECKING|LOAN", message = "Account type must be SAVINGS, CHECKING, or LOAN") // Example types
    private String accountType;

    @NotNull(message = "Initial deposit cannot be null")
    @DecimalMin(value = "0.00", message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
}