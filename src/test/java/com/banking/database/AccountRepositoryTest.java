package com.banking.database;

import com.banking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountRepositoryTest {

    private UserRepository userRepository;
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        dbManager.initializeSchema();
        userRepository = new UserRepository(dbManager);
        accountRepository = new AccountRepository(dbManager);

        userRepository.createUser(new User("U9001", "Alice Smith", UserRole.CUSTOMER));
    }

    @Test
    @DisplayName("Test account creation and lookup")
    void testCreateAndFindAccount() {
        Account account = new Account("ACC90001", "U9001", AccountType.SAVINGS, 10000.0);
        accountRepository.createAccount(account);

        assertTrue(account.getId() > 0);

        Optional<Account> found = accountRepository.findByAccountNumber("ACC90001");
        assertTrue(found.isPresent());
        assertEquals("U9001", found.get().getUserId());
        assertEquals(10000.0, found.get().getBalance());
        assertEquals(AccountStatus.ACTIVE, found.get().getStatus());
    }

    @Test
    @DisplayName("Test updating account balance")
    void testUpdateBalance() {
        Account account = new Account("ACC90001", "U9001", AccountType.SAVINGS, 10000.0);
        accountRepository.createAccount(account);

        accountRepository.updateBalance("ACC90001", 15000.0);

        Optional<Account> updated = accountRepository.findByAccountNumber("ACC90001");
        assertTrue(updated.isPresent());
        assertEquals(15000.0, updated.get().getBalance());
    }

    @Test
    @DisplayName("Test updating account status")
    void testUpdateStatus() {
        Account account = new Account("ACC90001", "U9001", AccountType.SAVINGS, 10000.0);
        accountRepository.createAccount(account);

        accountRepository.updateStatus("ACC90001", AccountStatus.BLOCKED);

        Optional<Account> updated = accountRepository.findByAccountNumber("ACC90001");
        assertTrue(updated.isPresent());
        assertEquals(AccountStatus.BLOCKED, updated.get().getStatus());
    }
}
