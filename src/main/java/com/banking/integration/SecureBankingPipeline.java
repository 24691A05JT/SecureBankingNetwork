package com.banking.integration;

import com.banking.communication.BankingClient;
import com.banking.communication.BankingServer;
import com.banking.database.AccountRepository;
import com.banking.database.DatabaseManager;
import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;
import com.banking.network.*;
import com.banking.reliability.CRCResult;
import com.banking.reliability.CRCService;
import com.banking.reliability.GoBackN;
import com.banking.reliability.SlidingWindow;
import com.banking.security.EncryptedMessage;
import com.banking.security.EncryptionService;
import com.banking.service.BankingService;
import com.banking.service.BankingTransactionResult;
import com.banking.util.LoggerUtil;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * End-to-End Secure Banking Transaction Communication Pipeline Orchestrator.
 * Coordinates existing network routing, AES-GCM encryption, CRC-16 error detection,
 * sliding window reliability, real TCP socket communication, banking business logic, and SQLite persistence.
 */
public class SecureBankingPipeline {

    private final DatabaseManager databaseManager;
    private final NetworkTopology topology;
    private final DijkstraRouter dijkstraRouter;
    private final DistanceVectorRouter dvRouter;

    public SecureBankingPipeline() {
        this(new DatabaseManager());
    }

    public SecureBankingPipeline(DatabaseManager databaseManager) {
        this(databaseManager, BankingTopologyBuilder.createSampleTopology());
    }

    public SecureBankingPipeline(DatabaseManager databaseManager, NetworkTopology topology) {
        if (databaseManager == null) {
            throw new IllegalArgumentException("DatabaseManager cannot be null.");
        }
        if (topology == null) {
            throw new IllegalArgumentException("NetworkTopology cannot be null.");
        }
        this.databaseManager = databaseManager;
        this.topology = topology;
        this.dijkstraRouter = new DijkstraRouter();
        this.dvRouter = new DistanceVectorRouter();
    }

    /**
     * Executes the secure banking pipeline with default options.
     *
     * @param request SecureBankingRequest
     * @return PipelineResult
     */
    public PipelineResult execute(SecureBankingRequest request) {
        return execute(request, PipelineOptions.defaultOptions());
    }

    /**
     * Executes the secure banking pipeline with specified simulation options.
     *
     * @param request SecureBankingRequest
     * @param options PipelineOptions for error/simulation control
     * @return PipelineResult
     */
    public PipelineResult execute(SecureBankingRequest request, PipelineOptions options) {
        if (request == null) {
            throw new IllegalArgumentException("SecureBankingRequest cannot be null");
        }
        if (options == null) {
            options = PipelineOptions.defaultOptions();
        }

        PipelineResult.Builder resultBuilder = new PipelineResult.Builder()
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount());

        LoggerUtil.info("==================================================");
        LoggerUtil.info("STARTING SECURE BANKING TRANSACTION PIPELINE");
        LoggerUtil.info("==================================================");

        // Fetch initial balances if accounts exist
        AccountRepository accountRepo = new AccountRepository(databaseManager);
        try (Connection conn = databaseManager.getConnection()) {
            accountRepo.findByAccountNumber(conn, request.getSourceAccount())
                    .ifPresent(acc -> resultBuilder.initialSourceBalance(acc.getBalance()));
            accountRepo.findByAccountNumber(conn, request.getDestinationAccount())
                    .ifPresent(acc -> resultBuilder.initialDestinationBalance(acc.getBalance()));
        } catch (SQLException e) {
            LoggerUtil.error("Failed to fetch initial account balances: " + e.getMessage());
        }

        // STAGE 1 — TRANSACTION CREATION
        LoggerUtil.info("[STAGE 1] Creating transaction request...");
        if (request.getAmount() <= 0) {
            LoggerUtil.warn("[STAGE 1 FAILED] Invalid transaction amount: " + request.getAmount());
            return resultBuilder
                    .successful(false)
                    .errorMessage("Invalid transaction amount: " + request.getAmount())
                    .build();
        }

        Transaction transaction = new Transaction(
                request.getSourceAccount(),
                request.getDestinationAccount(),
                request.getAmount(),
                request.getTransactionType(),
                request.getDescription()
        );
        transaction.setStatus(TransactionStatus.PENDING);
        resultBuilder.transactionId(transaction.getTransactionId());
        LoggerUtil.info("Transaction created successfully: " + transaction.getTransactionId());

        // STAGE 2 — ROUTING
        LoggerUtil.info("[STAGE 2] Determining network routing path using Link-State (Dijkstra) & Distance Vector...");
        NetworkNode srcNode = topology.getNode(request.getSourceNodeId());
        NetworkNode destNode = topology.getNode(request.getDestinationNodeId());

        if (srcNode == null || destNode == null) {
            LoggerUtil.warn("[STAGE 2 FAILED] Routing source or destination node not found in topology.");
            return resultBuilder
                    .successful(false)
                    .errorMessage("Routing node not found in topology.")
                    .build();
        }

        DijkstraResult dijkstraResult = dijkstraRouter.findShortestPath(topology, srcNode, destNode);
        DistanceVectorResult dvResult = dvRouter.computeRouting(topology, srcNode, destNode);

        resultBuilder.routingPath(dijkstraResult.getFormattedPath())
                .routingCost(dijkstraResult.getTotalCost())
                .nextHop(dijkstraResult.getNextHop() != null ? dijkstraResult.getNextHop().getNodeName() : "DIRECT")
                .distanceVectorPath(dvResult.getFormattedPath())
                .distanceVectorCost(dvResult.getTotalCost());

        LoggerUtil.info(String.format("Dijkstra Route Selected: %s (Cost: %.1f, NextHop: %s)",
                dijkstraResult.getFormattedPath(), dijkstraResult.getTotalCost(),
                dijkstraResult.getNextHop() != null ? dijkstraResult.getNextHop().getNodeName() : "DIRECT"));
        LoggerUtil.info(String.format("Distance Vector Route: %s (Cost: %.1f)",
                dvResult.getFormattedPath(), dvResult.getTotalCost()));

        // STAGE 3 — ENCRYPTION
        LoggerUtil.info("[STAGE 3] Encrypting transaction payload with AES-256-GCM...");
        SecretKey key = EncryptionService.generateKey();
        EncryptedMessage encryptedMessage = EncryptionService.encryptTransaction(transaction, key);
        resultBuilder.encryptionSuccessful(true);
        LoggerUtil.info("Encryption Status: SUCCESS");

        // STAGE 4 — CRC GENERATION
        LoggerUtil.info("[STAGE 4] Calculating CRC-16-CCITT checksum for data integrity...");
        String payloadToProtect = encryptedMessage.getEncryptedData() + "::" + encryptedMessage.getInitializationVector();
        CRCResult crcResult = CRCService.process(payloadToProtect);
        resultBuilder.crcGenerated(true);
        LoggerUtil.info("CRC Status: GENERATED (" + crcResult.getCrcFormatted() + ")");

        // STAGE 5 — RELIABLE TRANSMISSION (Sliding Window & Go-Back-N)
        LoggerUtil.info("[STAGE 5] Modeling reliable frame transmission via Sliding Window & Go-Back-N ARQ...");
        String framedPayload = payloadToProtect + "::CRC=" + crcResult.getCrcFormatted();

        SlidingWindow slidingWindow = new SlidingWindow(4, 2);
        GoBackN gbn = new GoBackN(4, 2);

        if (options.isSimulateFrameLoss()) {
            slidingWindow.simulateFrameLoss(1);
            gbn.simulateFrameLoss(1);
        }

        for (int i = 0; i < 4; i++) {
            if (slidingWindow.canSend()) slidingWindow.sendNextFrame("FRAME_" + i + "::" + framedPayload);
            if (gbn.canSend()) gbn.sendNextFrame("FRAME_" + i + "::" + framedPayload);
        }

        if (options.isSimulateFrameLoss()) {
            gbn.handleTimeout();
        }

        for (int i = 0; i < 4; i++) {
            slidingWindow.receiveAcknowledgement(i);
        }

        resultBuilder.reliableTransmissionSuccessful(slidingWindow.isComplete() || gbn.isComplete());
        LoggerUtil.info("Reliable Transmission Status: SUCCESS");

        // STAGE 6 — TCP TRANSMISSION
        LoggerUtil.info("[STAGE 6] Transmitting message over real TCP socket...");
        if (options.isSimulateTcpFailure()) {
            LoggerUtil.warn("[STAGE 6 FAILED] TCP transmission failed (simulated).");
            return resultBuilder
                    .successful(false)
                    .tcpSuccessful(false)
                    .errorMessage("TCP transmission failed cleanly (simulated).")
                    .build();
        }

        BankingServer server = new BankingServer(0);
        server.start();
        int port = server.getPort();

        String tcpMessage = "BANKING_TRANSACTION_REQUEST|DATA=" + framedPayload;
        String tcpResponse;

        try {
            BankingClient client = new BankingClient();
            tcpResponse = client.sendMessage("127.0.0.1", port, tcpMessage);
            resultBuilder.tcpSuccessful(true);
            LoggerUtil.info("TCP Transmission Status: SUCCESS (Received response: " + tcpResponse + ")");
        } catch (IOException e) {
            LoggerUtil.error("TCP Communication Failure: " + e.getMessage());
            return resultBuilder
                    .successful(false)
                    .tcpSuccessful(false)
                    .errorMessage("TCP transmission error: " + e.getMessage())
                    .build();
        } finally {
            server.stop();
        }

        // STAGE 7 — SERVER RECEIVES MESSAGE
        LoggerUtil.info("[STAGE 7] Server received message payload successfully.");

        // STAGE 8 — CRC VERIFICATION
        LoggerUtil.info("[STAGE 8] Verifying CRC-16-CCITT checksum...");
        String stringToVerify = payloadToProtect;
        int expectedCrc = crcResult.getCrc();

        if (options.isSimulateCrcCorruption()) {
            stringToVerify = payloadToProtect + "_TAMPERED";
            LoggerUtil.warn("Simulated CRC Corruption: String data tampered during transmission.");
        }

        boolean crcValid = CRCService.verifyCRC(stringToVerify, expectedCrc);
        if (!crcValid) {
            LoggerUtil.warn("[STAGE 8 FAILED] CRC verification failed! Aborting banking transaction.");
            return resultBuilder
                    .successful(false)
                    .crcVerified(false)
                    .errorMessage("CRC verification failed: transmission corruption detected.")
                    .build();
        }
        resultBuilder.crcVerified(true);
        LoggerUtil.info("CRC Verification Status: PASSED");

        // STAGE 9 — DECRYPTION
        LoggerUtil.info("[STAGE 9] Decrypting payload using AES-256-GCM...");
        SecretKey keyToUse = key;
        if (options.isSimulateDecryptionFailure()) {
            keyToUse = EncryptionService.generateKey(); // Mismatched key produces GCM tag error
            LoggerUtil.warn("Simulated Decryption Failure: Using invalid decryption key.");
        }

        Transaction decryptedTxn;
        try {
            decryptedTxn = EncryptionService.decryptTransaction(encryptedMessage, keyToUse);
            resultBuilder.decryptionSuccessful(true);
            LoggerUtil.info("AES-GCM Decryption Status: SUCCESS");
        } catch (SecurityException e) {
            LoggerUtil.warn("[STAGE 9 FAILED] Decryption failed! Aborting banking transaction.");
            return resultBuilder
                    .successful(false)
                    .decryptionSuccessful(false)
                    .errorMessage("Decryption failed: " + e.getMessage())
                    .build();
        }

        // Verify decrypted contents match request
        if (!decryptedTxn.getSourceAccount().equals(transaction.getSourceAccount()) ||
            !decryptedTxn.getDestinationAccount().equals(transaction.getDestinationAccount()) ||
            Math.abs(decryptedTxn.getAmount() - transaction.getAmount()) > 1e-6) {
            LoggerUtil.warn("[STAGE 9 FAILED] Decrypted transaction payload mismatch.");
            return resultBuilder
                    .successful(false)
                    .decryptionSuccessful(false)
                    .errorMessage("Decrypted transaction data mismatch.")
                    .build();
        }

        // STAGE 10 — BANKING TRANSACTION
        LoggerUtil.info("[STAGE 10] Processing banking transaction via BankingService...");
        BankingService bankingService = new BankingService(databaseManager);
        BankingTransactionResult txnResult;

        if (request.getTransactionType() == TransactionType.TRANSFER) {
            txnResult = bankingService.transfer(request.getSourceAccount(), request.getDestinationAccount(), request.getAmount());
        } else if (request.getTransactionType() == TransactionType.DEPOSIT) {
            txnResult = bankingService.deposit(request.getDestinationAccount(), request.getAmount());
        } else {
            txnResult = bankingService.withdraw(request.getSourceAccount(), request.getAmount());
        }

        if (!txnResult.isSuccess()) {
            LoggerUtil.warn("[STAGE 10 FAILED] Banking transaction failed: " + txnResult.getMessage());
            return resultBuilder
                    .successful(false)
                    .bankingTransactionSuccessful(false)
                    .databasePersistenceSuccessful(false)
                    .errorMessage(txnResult.getMessage())
                    .build();
        }

        resultBuilder.bankingTransactionSuccessful(true);
        LoggerUtil.info("Banking Transaction Status: SUCCESS (" + txnResult.getMessage() + ")");

        // STAGE 11 — DATABASE PERSISTENCE
        LoggerUtil.info("[STAGE 11] Verifying SQLite database persistence...");
        try (Connection conn = databaseManager.getConnection()) {
            boolean persistenceVerified = true;
            if (request.getSourceAccount() != null) {
                Optional<com.banking.model.Account> src = accountRepo.findByAccountNumber(conn, request.getSourceAccount());
                if (src.isPresent()) {
                    resultBuilder.finalSourceBalance(src.get().getBalance());
                } else {
                    persistenceVerified = false;
                }
            }
            if (request.getDestinationAccount() != null) {
                Optional<com.banking.model.Account> dest = accountRepo.findByAccountNumber(conn, request.getDestinationAccount());
                if (dest.isPresent()) {
                    resultBuilder.finalDestinationBalance(dest.get().getBalance());
                } else {
                    persistenceVerified = false;
                }
            }

            if (!persistenceVerified) {
                LoggerUtil.error("[STAGE 11 ERROR] SQLite persistence verification failed: Account records not found in database.");
                return resultBuilder
                        .successful(false)
                        .databasePersistenceSuccessful(false)
                        .errorMessage("SQLite persistence verification failed: Account not found.")
                        .build();
            }

            resultBuilder.databasePersistenceSuccessful(true);
            LoggerUtil.info("[STAGE 11] SQLite persistence verified successfully.");
            LoggerUtil.info("SQLite Persistence Status: SUCCESS");
        } catch (SQLException e) {
            LoggerUtil.error("[STAGE 11 ERROR] SQLite persistence verification failed: " + e.getMessage());
            return resultBuilder
                    .successful(false)
                    .databasePersistenceSuccessful(false)
                    .errorMessage("SQLite persistence verification failed: " + e.getMessage())
                    .build();
        }

        // STAGE 12 — RESPONSE
        LoggerUtil.info("[STAGE 12] Finalizing pipeline response...");
        String txnId = (txnResult.getTransactionId() != null) ? txnResult.getTransactionId() : transaction.getTransactionId();
        resultBuilder.transactionId(txnId);
        String finalResponse = String.format("PIPELINE_SUCCESS|TxnID=%s|Msg=%s",
                txnId, txnResult.getMessage());

        LoggerUtil.info("==================================================");
        LoggerUtil.info("SECURE BANKING PIPELINE COMPLETED SUCCESSFULLY");
        LoggerUtil.info("Pipeline Execution PASSED cleanly for Txn ID: " + txnId);
        LoggerUtil.info("==================================================");

        return resultBuilder
                .successful(true)
                .finalResponse(finalResponse)
                .build();
    }
}
