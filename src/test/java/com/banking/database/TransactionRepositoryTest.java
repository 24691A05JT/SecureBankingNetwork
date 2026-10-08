package com.banking.database;

import com.banking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRepositoryTest {

    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        dbManager.initializeSchema();
        transactionRepository = new TransactionRepository(dbManager);
    }

    @Test
    @DisplayName("Test saving and retrieving transaction")
    void testSaveAndFindTransaction() {
        Transaction txn = new Transaction("ACC10001", "ACC10002", 5000.0, TransactionType.TRANSFER, "Test transfer");
        txn.setStatus(TransactionStatus.SUCCESS);

        transactionRepository.saveTransaction(txn);
        assertTrue(txn.getId() > 0);

        Optional<Transaction> found = transactionRepository.findTransactionById(txn.getTransactionId());
        assertTrue(found.isPresent());
        assertEquals("ACC10001", found.get().getSourceAccount());
        assertEquals("ACC10002", found.get().getDestinationAccount());
        assertEquals(5000.0, found.get().getAmount());
        assertEquals(TransactionStatus.SUCCESS, found.get().getStatus());
    }

    @Test
    @DisplayName("Test searching transactions by account number")
    void testFindTransactionsByAccount() {
        Transaction t1 = new Transaction("ACC10001", "ACC10002", 1000.0, TransactionType.TRANSFER, "Txn 1");
        t1.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.saveTransaction(t1);

        Transaction t2 = new Transaction("ACC10003", "ACC10001", 2000.0, TransactionType.TRANSFER, "Txn 2");
        t2.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.saveTransaction(t2);

        List<Transaction> acc1Txns = transactionRepository.findTransactionsByAccount("ACC10001");
        assertEquals(2, acc1Txns.size());
    }
}
