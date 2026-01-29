package com.banking.easbank.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "login_history")
public class LoginHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private LocalDateTime loginDateTime;
    private boolean successful;
    private String ipAddress;
    
    public LoginHistory() {}
    
    public LoginHistory(String email, LocalDateTime loginDateTime, boolean successful) {
        this.email = email;
        this.loginDateTime = loginDateTime;
        this.successful = successful;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getLoginDateTime() { return loginDateTime; }
    public void setLoginDateTime(LocalDateTime loginDateTime) { this.loginDateTime = loginDateTime; }
    
    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}