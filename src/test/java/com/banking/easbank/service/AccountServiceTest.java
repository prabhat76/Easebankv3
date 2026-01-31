package com.banking.easbank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banking.easbank.entity.Account;
import com.banking.easbank.entity.User;
import com.banking.easbank.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

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
    void openAccount_WhenUserExists_ShouldCreateAccount() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.openAccount(1L, "SAVINGS", new BigDecimal("1000.00"));

        assertNotNull(result);
        assertEquals("SAVINGS", result.getAccountType());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        assertEquals(testUser, result.getUser());
        verify(userService).getUserById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void openAccount_WhenUserNotExists_ShouldThrowException() {
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> accountService.openAccount(999L, "SAVINGS", new BigDecimal("1000.00")));

        assertEquals("User not found", exception.getMessage());
        verify(userService).getUserById(999L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountById_WhenAccountExists_ShouldReturnAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        Optional<Account> result = accountService.getAccountById(1L);

        assertTrue(result.isPresent());
        assertEquals(testAccount.getAccountNumber(), result.get().getAccountNumber());
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccountById_WhenAccountNotExists_ShouldReturnEmpty() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Account> result = accountService.getAccountById(999L);

        assertFalse(result.isPresent());
        verify(accountRepository).findById(999L);
    }

    @Test
    void openAccount_ShouldGenerateUniqueAccountNumber() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(1L);
            return account;
        });

        Account result = accountService.openAccount(1L, "CURRENT", new BigDecimal("5000.00"));

        assertNotNull(result.getAccountNumber());
        assertTrue(result.getAccountNumber().startsWith("ACC"));
        assertEquals("CURRENT", result.getAccountType());
        assertEquals(new BigDecimal("5000.00"), result.getBalance());
    }

    @Test
    void openAccount_WithZeroDeposit_ShouldCreateAccount() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.openAccount(1L, "SAVINGS", BigDecimal.ZERO);

        assertNotNull(result);
        verify(accountRepository).save(any(Account.class));
    }
}