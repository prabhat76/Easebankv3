package com.banking.easbank.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.banking.easbank.entity.Account;
import java.util.Optional;
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    // Explicitly declare findById to ensure null-safety and type compatibility
    @Override
    Optional<Account> findById(Long id);
}
