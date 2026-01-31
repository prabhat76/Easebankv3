package com.banking.easbank.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.banking.easbank.entity.User;
import com.banking.easbank.repository.UserRepository;
import com.banking.easbank.repository.AccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class BankingApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeUserJourney_CreateUserLoginAndOpenAccount() throws Exception {
        // Step 1: Create a new user
        User newUser = new User();
        newUser.setEmail("integration@test.com");
        newUser.setFirstName("Integration");
        newUser.setLastName("Test");
        newUser.setPhone("1234567890");
        newUser.setPassword("testpassword");

        MvcResult createUserResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andReturn();

        // Extract user ID from response
        String createUserResponse = createUserResult.getResponse().getContentAsString();
        JsonNode userNode = objectMapper.readTree(createUserResponse);
        Long userId = userNode.get("id").asLong();

        // Step 2: Login with the created user
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "integration@test.com");
        loginRequest.put("password", "testpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.email").value("integration@test.com"));

        // Step 3: Open an account for the user
        Map<String, Object> accountRequest = new HashMap<>();
        accountRequest.put("userId", userId);
        accountRequest.put("accountType", "SAVINGS");
        accountRequest.put("initialDeposit", 1000.00);

        MvcResult openAccountResult = mockMvc.perform(post("/api/accounts/AccountOpening")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account opened successfully"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.userId").value(userId))
                .andReturn();

        // Extract account ID from response
        String accountResponse = openAccountResult.getResponse().getContentAsString();
        JsonNode accountNode = objectMapper.readTree(accountResponse);
        Long accountId = accountNode.get("accountId").asLong();

        // Step 4: Retrieve account details
        mockMvc.perform(get("/api/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.userId").value(userId));

        // Step 5: Verify data persistence
        assertTrue(userRepository.findById(userId).isPresent());
        assertTrue(accountRepository.findById(accountId).isPresent());
    }

    @Test
    void invalidLogin_ShouldReturnUnauthorized() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "nonexistent@test.com");
        loginRequest.put("password", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }
}