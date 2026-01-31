package com.banking.easbank.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.banking.easbank.entity.Account;
import com.banking.easbank.entity.User;
import com.banking.easbank.service.AccountService;
import com.banking.easbank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("ACC123456789");
        testAccount.setAccountType("SAVINGS");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setUser(testUser);
    }

    @Test
    void getAccount_WhenAccountExists_ShouldReturnAccount() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(Optional.of(testAccount));

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("ACC123456789"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(1000.00));

        verify(accountService).getAccountById(1L);
    }

    @Test
    void getAccount_WhenAccountNotExists_ShouldReturn404() throws Exception {
        when(accountService.getAccountById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found"));

        verify(accountService).getAccountById(999L);
    }

    @Test
    void openAccount_WithUserId_ShouldCreateAccount() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 1L);
        request.put("accountType", "SAVINGS");
        request.put("initialDeposit", 1000.00);

        when(accountService.openAccount(1L, "SAVINGS", new BigDecimal("1000.00")))
                .thenReturn(testAccount);

        mockMvc.perform(post("/api/accounts/AccountOpening")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account opened successfully"))
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("ACC123456789"));

        verify(accountService).openAccount(1L, "SAVINGS", new BigDecimal("1000.00"));
    }

    @Test
    void openAccount_WithEmail_ShouldCreateAccount() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("accountType", "CURRENT");
        request.put("initialDeposit", 5000.00);

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountService.openAccount(1L, "CURRENT", new BigDecimal("5000.00")))
                .thenReturn(testAccount);

        mockMvc.perform(post("/api/accounts/AccountOpening")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account opened successfully"));

        verify(userService).findByEmail("test@example.com");
        verify(accountService).openAccount(1L, "CURRENT", new BigDecimal("5000.00"));
    }

    @Test
    void openAccount_WithInvalidEmail_ShouldReturn404() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "nonexistent@example.com");
        request.put("accountType", "SAVINGS");
        request.put("initialDeposit", 1000.00);

        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/accounts/AccountOpening")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService).findByEmail("nonexistent@example.com");
        verify(accountService, never()).openAccount(anyLong(), anyString(), any(BigDecimal.class));
    }

    @Test
    void openAccount_WithMissingParameters_ShouldReturn400() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("accountType", "SAVINGS");
        request.put("initialDeposit", 1000.00);

        mockMvc.perform(post("/api/accounts/AccountOpening")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing parameter"));

        verify(accountService, never()).openAccount(anyLong(), anyString(), any(BigDecimal.class));
    }

    @Test
    void getBalance_ShouldReturnBalance() throws Exception {
        mockMvc.perform(get("/api/accounts/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("1"))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
}