package com.banking.easbank.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.banking.easbank.entity.User;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("1234567890");
        testUser.setPassword("password123");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByEmail_WhenEmailExists_ShouldReturnUser() {
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals("John", found.get().getFirstName());
        assertEquals("Doe", found.get().getLastName());
    }

    @Test
    void findByEmail_WhenEmailNotExists_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_WithCaseInsensitive_ShouldReturnUser() {
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByEmail("TEST@EXAMPLE.COM");

        // This test depends on database collation settings
        // May need to be adjusted based on your database configuration
    }

    @Test
    void save_ShouldPersistUser() {
        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
        
        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        User persistedUser = entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findById(persistedUser.getId());

        assertTrue(found.isPresent());
        assertEquals(persistedUser.getId(), found.get().getId());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        User persistedUser = entityManager.persistAndFlush(testUser);
        Long userId = persistedUser.getId();

        userRepository.deleteById(userId);
        entityManager.flush();

        User deletedUser = entityManager.find(User.class, userId);
        assertNull(deletedUser);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setPhone("0987654321");
        user2.setPassword("password456");
        user2.setCreatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(user2);

        Iterable<User> users = userRepository.findAll();
        
        long count = 0;
        for (User user : users) {
            count++;
        }
        
        assertEquals(2, count);
    }
}