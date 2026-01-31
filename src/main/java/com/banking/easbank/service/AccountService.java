package com.banking.easbank.service;
import java.math.BigDecimal;
import java.util.Random;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.easbank.entity.Account;
import com.banking.easbank.entity.User;
import com.banking.easbank.repository.AccountRepository;


@Service
@Transactional
public class AccountService {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountRepository accountRepository;

    public User getAccountHolderDetails(String accountNumber) {
        // Implementation to fetch account holder details using accountNumber
        return null; // Placeholder return statement
    }

  public Account openAccount(Long userId, String accountType, BigDecimal initialDeposit) {
    System.out.println("Opening account for userId: " + userId);
    
    Optional<User> userOpt = userService.getUserById(userId);
    System.out.println("User found: " + userOpt.isPresent());
    
    if (userOpt.isPresent()) {
        User user = userOpt.get();
        String accountNumber = generateUniqueAccountNumber();
        System.out.println("Generated account number: " + accountNumber);
        
        Account account = new Account();
        account.setUser(user);
        account.setAccountType(accountType);
        account.setBalance(initialDeposit);
        account.setAccountNumber(accountNumber);
        
        System.out.println("Saving account to database...");
        Account savedAccount = accountRepository.save(account);
        System.out.println("Account saved with ID: " + savedAccount.getId());
        
        return savedAccount;
    }
    throw new RuntimeException("User not found");
}


    private String generateUniqueAccountNumber() {
        return "ACC" + System.currentTimeMillis()+new Random().nextInt(); 
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }


    
}
