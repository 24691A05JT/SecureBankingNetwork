package com.banking.database;

import com.banking.model.User;
import com.banking.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        dbManager.initializeSchema();
        userRepository = new UserRepository(dbManager);
    }

    @Test
    @DisplayName("Test user creation and retrieval by userId")
    void testCreateAndFindUser() {
        User user = new User("U9001", "Alice Smith", UserRole.CUSTOMER);
        userRepository.createUser(user);

        assertTrue(user.getId() > 0);

        Optional<User> found = userRepository.findByUserId("U9001");
        assertTrue(found.isPresent());
        assertEquals("Alice Smith", found.get().getFullName());
        assertEquals(UserRole.CUSTOMER, found.get().getRole());
    }

    @Test
    @DisplayName("Test listing all users")
    void testFindAllUsers() {
        userRepository.createUser(new User("U9001", "Alice", UserRole.CUSTOMER));
        userRepository.createUser(new User("U9002", "Bob", UserRole.MANAGER));

        assertEquals(2, userRepository.findAllUsers().size());
    }
}
