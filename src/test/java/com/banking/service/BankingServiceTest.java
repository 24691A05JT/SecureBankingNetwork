package com.banking.service;

import com.banking.database.AccountRepository;
import com.banking.database.DatabaseManager;
import com.banking.database.UserRepository;
import com.banking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BankingServiceTest {

    private DatabaseManager dbManager;
    private AccountRepository accountRepo;
    private BankingService bankingService;

    @BeforeEach
    void setUp() {
        dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        dbManager.initializeSchema();

        UserRepository userRepo = new UserRepository(dbManager);
        userRepo.createUser(new User("U1001", "Customer 1", UserRole.CUSTOMER));
        userRepo.createUser(new User("U1002", "Customer 2", UserRole.CUSTOMER));

        accountRepo = new AccountRepository(dbManager);
        accountRepo.createAccount(new Account("ACC10001", "U1001", AccountType.SAVINGS, 50000.0));
        accountRepo.createAccount(new Account("ACC10002", "U1002", AccountType.CURRENT, 25000.0));

        bankingService = new BankingService(dbManager);
    }

    @Test
    @DisplayName("Test successful deposit")
    void testDepositSuccess() {
        BankingTransactionResult result = bankingService.deposit("ACC10001", 5000.0);

        assertTrue(result.isSuccess());
        Optional<Account> acc = accountRepo.findByAccountNumber("ACC10001");
        assertTrue(acc.isPresent());
        assertEquals(55000.0, acc.get().getBalance());
    }

    @Test
    @DisplayName("Test successful withdrawal")
    void testWithdrawSuccess() {
        BankingTransactionResult result = bankingService.withdraw("ACC10001", 10000.0);

        assertTrue(result.isSuccess());
        Optional<Account> acc = accountRepo.findByAccountNumber("ACC10001");
        assertTrue(acc.isPresent());
        assertEquals(40000.0, acc.get().getBalance());
    }

    @Test
    @DisplayName("Test successful atomic money transfer")
    void testTransferSuccess() {
        BankingTransactionResult result = bankingService.transfer("ACC10001", "ACC10002", 5000.0);

        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertEquals("Transfer completed successfully.", result.getMessage());

        Optional<Account> src = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> dest = accountRepo.findByAccountNumber("ACC10002");

        assertTrue(src.isPresent());
        assertTrue(dest.isPresent());
        assertEquals(45000.0, src.get().getBalance());
        assertEquals(30000.0, dest.get().getBalance());
    }

    @Test
    @DisplayName("Test transfer failure due to insufficient funds (balances unchanged)")
    void testTransferInsufficientFunds() {
        BankingTransactionResult result = bankingService.transfer("ACC10001", "ACC10002", 100000.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Insufficient funds"));

        Optional<Account> src = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> dest = accountRepo.findByAccountNumber("ACC10002");

        assertTrue(src.isPresent());
        assertTrue(dest.isPresent());
        assertEquals(50000.0, src.get().getBalance()); // Unchanged
        assertEquals(25000.0, dest.get().getBalance()); // Unchanged
    }

    @Test
    @DisplayName("Test transfer failure with invalid negative amount")
    void testTransferInvalidAmount() {
        BankingTransactionResult result = bankingService.transfer("ACC10001", "ACC10002", -500.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("greater than zero"));
    }

    @Test
    @DisplayName("Test transfer failure when source and destination accounts are identical")
    void testTransferSameAccount() {
        BankingTransactionResult result = bankingService.transfer("ACC10001", "ACC10001", 1000.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("cannot be the same"));
    }

    @Test
    @DisplayName("Test transfer failure when source account is blocked")
    void testTransferBlockedAccount() {
        accountRepo.updateStatus("ACC10001", AccountStatus.BLOCKED);

        BankingTransactionResult result = bankingService.transfer("ACC10001", "ACC10002", 1000.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("BLOCKED"));
    }

    @Test
    @DisplayName("Test transfer failure when destination account does not exist")
    void testTransferNonExistentDestination() {
        BankingTransactionResult result = bankingService.transfer("ACC10001", "ACC99999", 1000.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not found"));
    }
}
