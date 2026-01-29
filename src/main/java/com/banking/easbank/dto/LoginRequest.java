package com.banking.easbank.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "login_requests")
public class LoginRequest {

    private String email;
    private String password;
    private LocalDateTime loggedinDateTime;

    public LoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getLoggedinDateTime() {
        return loggedinDateTime;
    }

    public void setLoggedinDateTime(LocalDateTime loggedinDateTime) {
        this.loggedinDateTime = loggedinDateTime;
    }
}
