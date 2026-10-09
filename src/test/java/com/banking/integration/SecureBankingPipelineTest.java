package com.banking.integration;

import com.banking.database.AccountRepository;
import com.banking.database.DatabaseManager;
import com.banking.model.Account;
import com.banking.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecureBankingPipelineTest {

    private DatabaseManager dbManager;
    private AccountRepository accountRepo;
    private SecureBankingPipeline pipeline;

    @BeforeEach
    void setUp() {
        // Isolated in-memory SQLite database for repeatable unit tests
        dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        dbManager.initializeSchema();
        dbManager.seedSampleData();

        accountRepo = new AccountRepository(dbManager);
        pipeline = new SecureBankingPipeline(dbManager);
    }

    @Test
    @DisplayName("Test 1: Successful End-to-End Secure Banking Transaction Pipeline")
    void testSuccessfulEndToEndTransaction() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 5000.0,
                TransactionType.TRANSFER, "Integration Test Transfer");

        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertNotNull(result.getTransactionId());
        assertFalse(result.getTransactionId().isEmpty());

        assertTrue(result.isEncryptionSuccessful());
        assertTrue(result.isCrcGenerated());
        assertTrue(result.isCrcVerified());
        assertTrue(result.isReliableTransmissionSuccessful());
        assertTrue(result.isTcpSuccessful());
        assertTrue(result.isDecryptionSuccessful());
        assertTrue(result.isBankingTransactionSuccessful());
        assertTrue(result.isDatabasePersistenceSuccessful());

        assertEquals(50000.0, result.getInitialSourceBalance());
        assertEquals(45000.0, result.getFinalSourceBalance());
        assertEquals(25000.0, result.getInitialDestinationBalance());
        assertEquals(30000.0, result.getFinalDestinationBalance());

        Optional<Account> src = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> dest = accountRepo.findByAccountNumber("ACC10002");

        assertTrue(src.isPresent());
        assertTrue(dest.isPresent());
        assertEquals(45000.0, src.get().getBalance());
        assertEquals(30000.0, dest.get().getBalance());
    }

    @Test
    @DisplayName("Test 2: Routing Stage (Dijkstra and Distance Vector computation)")
    void testRoutingStage() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 1000.0,
                TransactionType.TRANSFER, "Routing Stage Test", "NODE-B1", "NODE-DB");

        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertNotNull(result.getRoutingPath());
        assertFalse(result.getRoutingPath().isEmpty());
        assertTrue(result.getRoutingCost() > 0);
        assertNotNull(result.getNextHop());

        assertNotNull(result.getDistanceVectorPath());
        assertTrue(result.getDistanceVectorCost() > 0);
        assertEquals(result.getRoutingCost(), result.getDistanceVectorCost(), 1e-6);
    }

    @Test
    @DisplayName("Test 3: Encryption Stage (AES-256-GCM)")
    void testEncryptionStage() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 500.0);
        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertTrue(result.isEncryptionSuccessful());
    }

    @Test
    @DisplayName("Test 4: CRC Generation Stage (CRC-16-CCITT)")
    void testCrcGenerationStage() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 500.0);
        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertTrue(result.isCrcGenerated());
    }

    @Test
    @DisplayName("Test 5: CRC Verification Stage")
    void testCrcVerificationStage() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 500.0);
        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertTrue(result.isCrcVerified());
    }

    @Test
    @DisplayName("Test 6: Reliable Frame Transmission Stage (Sliding Window & Go-Back-N)")
    void testReliableTransmissionStage() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 500.0);
        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertTrue(result.isReliableTransmissionSuccessful());
    }

    @Test
    @DisplayName("Test 7: TCP Transmission Stage (Socket communication)")
    void testTcpTransmissionStage() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 500.0);
        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertTrue(result.isTcpSuccessful());
    }

    @Test
    @DisplayName("Test 8: Decryption Stage (AES-256-GCM Payload Recovery)")
    void testSuccessfulDecryptionStage() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 500.0);
        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertTrue(result.isDecryptionSuccessful());
    }

    @Test
    @DisplayName("Test 9: Banking Transaction Stage (Atomic Balance Update)")
    void testSuccessfulBankingTransactionStage() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 2000.0);
        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertTrue(result.isBankingTransactionSuccessful());
        assertEquals(48000.0, result.getFinalSourceBalance());
        assertEquals(27000.0, result.getFinalDestinationBalance());
    }

    @Test
    @DisplayName("Test 10: Database Persistence Stage")
    void testDatabasePersistenceStage() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 1000.0);
        PipelineResult result = pipeline.execute(request);

        assertTrue(result.isSuccessful());
        assertTrue(result.isDatabasePersistenceSuccessful());

        Optional<Account> src = accountRepo.findByAccountNumber("ACC10001");
        assertTrue(src.isPresent());
        assertEquals(49000.0, src.get().getBalance());
    }

    @Test
    @DisplayName("Test 11: Invalid Amount Failure (Pipeline Safety)")
    void testInvalidAmountFailure() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", -500.0);
        PipelineResult result = pipeline.execute(request);

        assertFalse(result.isSuccessful());
        assertTrue(result.getErrorMessage().contains("Invalid transaction amount"));

        // Balances must remain completely unchanged
        Optional<Account> src = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> dest = accountRepo.findByAccountNumber("ACC10002");
        assertTrue(src.isPresent());
        assertTrue(dest.isPresent());
        assertEquals(50000.0, src.get().getBalance());
        assertEquals(25000.0, dest.get().getBalance());
    }

    @Test
    @DisplayName("Test 12: Insufficient Balance Failure (Pipeline Safety)")
    void testInsufficientBalanceFailure() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 999999.0);
        PipelineResult result = pipeline.execute(request);

        assertFalse(result.isSuccessful());
        assertFalse(result.isBankingTransactionSuccessful());

        // Balances must remain completely unchanged
        Optional<Account> src = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> dest = accountRepo.findByAccountNumber("ACC10002");
        assertTrue(src.isPresent());
        assertTrue(dest.isPresent());
        assertEquals(50000.0, src.get().getBalance());
        assertEquals(25000.0, dest.get().getBalance());
    }

    @Test
    @DisplayName("Test 13: CRC Corruption Failure Safety")
    void testCrcCorruptionFailure() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 5000.0);
        PipelineOptions options = PipelineOptions.withCrcCorruption();

        PipelineResult result = pipeline.execute(request, options);

        assertFalse(result.isSuccessful());
        assertFalse(result.isCrcVerified());

        // Banking transaction must NOT be processed and balances must be untouched
        Optional<Account> src = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> dest = accountRepo.findByAccountNumber("ACC10002");
        assertTrue(src.isPresent());
        assertTrue(dest.isPresent());
        assertEquals(50000.0, src.get().getBalance());
        assertEquals(25000.0, dest.get().getBalance());
    }

    @Test
    @DisplayName("Test 14: Decryption Failure Safety")
    void testDecryptionFailure() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 5000.0);
        PipelineOptions options = PipelineOptions.withDecryptionFailure();

        PipelineResult result = pipeline.execute(request, options);

        assertFalse(result.isSuccessful());
        assertFalse(result.isDecryptionSuccessful());

        // Balances must remain completely unchanged
        Optional<Account> src = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> dest = accountRepo.findByAccountNumber("ACC10002");
        assertTrue(src.isPresent());
        assertTrue(dest.isPresent());
        assertEquals(50000.0, src.get().getBalance());
        assertEquals(25000.0, dest.get().getBalance());
    }

    @Test
    @DisplayName("Test 15: TCP Socket Transmission Failure Safety")
    void testTcpFailure() {
        SecureBankingRequest request = new SecureBankingRequest("ACC10001", "ACC10002", 5000.0);
        PipelineOptions options = PipelineOptions.withTcpFailure();

        PipelineResult result = pipeline.execute(request, options);

        assertFalse(result.isSuccessful());
        assertFalse(result.isTcpSuccessful());

        // Balances must remain completely unchanged
        Optional<Account> src = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> dest = accountRepo.findByAccountNumber("ACC10002");
        assertTrue(src.isPresent());
        assertTrue(dest.isPresent());
        assertEquals(50000.0, src.get().getBalance());
        assertEquals(25000.0, dest.get().getBalance());
    }
}
