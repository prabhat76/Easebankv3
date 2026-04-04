package com.banking.easbank.service;

import com.banking.easbank.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User saveUser(User user);
    void deleteUser(Long id);
    Optional<User> findByEmail(String email);
}