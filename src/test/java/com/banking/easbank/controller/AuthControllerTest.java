package com.banking.easbank.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.banking.easbank.dto.LoginRequest;
import com.banking.easbank.entity.LoginHistory;
import com.banking.easbank.entity.User;
import com.banking.easbank.repository.LoginHistoryRepository;
import com.banking.easbank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPassword("password123");
        testUser.setCreatedAt(LocalDateTime.now());

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnSuccess() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.logintime").exists());

        verify(userService).findByEmail("test@example.com");
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void login_WithInvalidEmail_ShouldReturnUnauthorized() throws Exception {
        when(userService.findByEmail("invalid@example.com")).thenReturn(Optional.empty());
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("invalid@example.com");
        invalidRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"))
                .andExpect(jsonPath("$.message").value("Email or password is incorrect"));

        verify(userService).findByEmail("invalid@example.com");
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnUnauthorized() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"))
                .andExpect(jsonPath("$.message").value("Email or password is incorrect"));

        verify(userService).findByEmail("test@example.com");
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void login_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        LoginRequest emptyEmailRequest = new LoginRequest();
        emptyEmailRequest.setEmail("");
        emptyEmailRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyEmailRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        LoginRequest emptyPasswordRequest = new LoginRequest();
        emptyPasswordRequest.setEmail("test@example.com");
        emptyPasswordRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyPasswordRequest)))
                .andExpect(status().isUnauthorized());
    }
}