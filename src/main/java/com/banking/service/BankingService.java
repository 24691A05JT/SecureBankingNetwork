package com.banking.service;

import com.banking.database.AccountRepository;
import com.banking.database.DatabaseManager;
import com.banking.database.TransactionRepository;
import com.banking.model.*;
import com.banking.util.LoggerUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Service class handling core banking business operations (Deposit, Withdrawal, Transfer)
 * with ACID transaction guarantees and rollback safety.
 */
public class BankingService {

    private final DatabaseManager databaseManager;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankingService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.accountRepository = new AccountRepository(databaseManager);
        this.transactionRepository = new TransactionRepository(databaseManager);
    }

    /**
     * Deposits money into a bank account.
     *
     * @param accountNumber target account number
     * @param amount deposit amount (> 0)
     * @return BankingTransactionResult
     */
    public BankingTransactionResult deposit(String accountNumber, double amount) {
        if (amount <= 0) {
            return BankingTransactionResult.failure("Deposit amount must be greater than zero, got: " + amount);
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return BankingTransactionResult.failure("Account number cannot be empty.");
        }

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Account> accountOpt = accountRepository.findByAccountNumber(conn, accountNumber);
                if (accountOpt.isEmpty()) {
                    conn.rollback();
                    return BankingTransactionResult.failure("Account '" + accountNumber + "' not found.");
                }

                Account account = accountOpt.get();
                if (account.getStatus() != AccountStatus.ACTIVE) {
                    conn.rollback();
                    return BankingTransactionResult.failure("Account '" + accountNumber + "' is " + account.getStatus() + ".");
                }

                double newBalance = account.getBalance() + amount;
                accountRepository.updateBalance(conn, accountNumber, newBalance);

                Transaction txn = new Transaction(null, accountNumber, amount, TransactionType.DEPOSIT, "Cash Deposit");
                txn.setStatus(TransactionStatus.SUCCESS);
                transactionRepository.saveTransaction(conn, txn);

                conn.commit();
                return BankingTransactionResult.success(txn, String.format("Successfully deposited ₹%.2f into %s", amount, accountNumber));
            } catch (Exception e) {
                conn.rollback();
                return BankingTransactionResult.failure("Deposit failed: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return BankingTransactionResult.failure("Database error: " + e.getMessage());
        }
    }

    /**
     * Withdraws money from a bank account.
     *
     * @param accountNumber source account number
     * @param amount withdrawal amount (> 0)
     * @return BankingTransactionResult
     */
    public BankingTransactionResult withdraw(String accountNumber, double amount) {
        if (amount <= 0) {
            return BankingTransactionResult.failure("Withdrawal amount must be greater than zero, got: " + amount);
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return BankingTransactionResult.failure("Account number cannot be empty.");
        }

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Account> accountOpt = accountRepository.findByAccountNumber(conn, accountNumber);
                if (accountOpt.isEmpty()) {
                    conn.rollback();
                    return BankingTransactionResult.failure("Account '" + accountNumber + "' not found.");
                }

                Account account = accountOpt.get();
                if (account.getStatus() != AccountStatus.ACTIVE) {
                    conn.rollback();
                    return BankingTransactionResult.failure("Account '" + accountNumber + "' is " + account.getStatus() + ".");
                }

                if (account.getBalance() < amount) {
                    Transaction txn = new Transaction(accountNumber, null, amount, TransactionType.WITHDRAWAL, "Failed Withdrawal: Insufficient Funds");
                    txn.setStatus(TransactionStatus.FAILED);
                    transactionRepository.saveTransaction(conn, txn);
                    conn.commit();
                    return BankingTransactionResult.failure(txn, "Insufficient balance in account " + accountNumber);
                }

                double newBalance = account.getBalance() - amount;
                accountRepository.updateBalance(conn, accountNumber, newBalance);

                Transaction txn = new Transaction(accountNumber, null, amount, TransactionType.WITHDRAWAL, "Cash Withdrawal");
                txn.setStatus(TransactionStatus.SUCCESS);
                transactionRepository.saveTransaction(conn, txn);

                conn.commit();
                return BankingTransactionResult.success(txn, String.format("Successfully withdrew ₹%.2f from %s", amount, accountNumber));
            } catch (Exception e) {
                conn.rollback();
                return BankingTransactionResult.failure("Withdrawal failed: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return BankingTransactionResult.failure("Database error: " + e.getMessage());
        }
    }

    /**
     * Executes an atomic money transfer between source and destination accounts inside a database transaction.
     * Guarantees all-or-nothing (ACID) execution.
     *
     * @param sourceAccNo source account number
     * @param destAccNo destination account number
     * @param amount transfer amount (> 0)
     * @return BankingTransactionResult
     */
    public BankingTransactionResult transfer(String sourceAccNo, String destAccNo, double amount) {
        if (sourceAccNo == null || sourceAccNo.trim().isEmpty()) {
            return BankingTransactionResult.failure("Source account number cannot be empty.");
        }
        if (destAccNo == null || destAccNo.trim().isEmpty()) {
            return BankingTransactionResult.failure("Destination account number cannot be empty.");
        }
        if (sourceAccNo.trim().equalsIgnoreCase(destAccNo.trim())) {
            return BankingTransactionResult.failure("Source and destination account numbers cannot be the same.");
        }
        if (amount <= 0) {
            return BankingTransactionResult.failure("Transfer amount must be greater than zero, got: " + amount);
        }

        String srcStr = sourceAccNo.trim();
        String destStr = destAccNo.trim();

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Account> srcOpt = accountRepository.findByAccountNumber(conn, srcStr);
                Optional<Account> destOpt = accountRepository.findByAccountNumber(conn, destStr);

                if (srcOpt.isEmpty()) {
                    Transaction txn = new Transaction(srcStr, destStr, amount, TransactionType.TRANSFER, "Failed Transfer: Source Account Not Found");
                    txn.setStatus(TransactionStatus.FAILED);
                    transactionRepository.saveTransaction(conn, txn);
                    conn.commit();
                    return BankingTransactionResult.failure(txn, "Source account '" + srcStr + "' not found.");
                }

                if (destOpt.isEmpty()) {
                    Transaction txn = new Transaction(srcStr, destStr, amount, TransactionType.TRANSFER, "Failed Transfer: Destination Account Not Found");
                    txn.setStatus(TransactionStatus.FAILED);
                    transactionRepository.saveTransaction(conn, txn);
                    conn.commit();
                    return BankingTransactionResult.failure(txn, "Destination account '" + destStr + "' not found.");
                }

                Account srcAcc = srcOpt.get();
                Account destAcc = destOpt.get();

                if (srcAcc.getStatus() != AccountStatus.ACTIVE) {
                    Transaction txn = new Transaction(srcStr, destStr, amount, TransactionType.TRANSFER, "Failed Transfer: Source Account Blocked");
                    txn.setStatus(TransactionStatus.FAILED);
                    transactionRepository.saveTransaction(conn, txn);
                    conn.commit();
                    return BankingTransactionResult.failure(txn, "Source account '" + srcStr + "' is " + srcAcc.getStatus() + ".");
                }

                if (destAcc.getStatus() != AccountStatus.ACTIVE) {
                    Transaction txn = new Transaction(srcStr, destStr, amount, TransactionType.TRANSFER, "Failed Transfer: Destination Account Blocked");
                    txn.setStatus(TransactionStatus.FAILED);
                    transactionRepository.saveTransaction(conn, txn);
                    conn.commit();
                    return BankingTransactionResult.failure(txn, "Destination account '" + destStr + "' is " + destAcc.getStatus() + ".");
                }

                if (srcAcc.getBalance() < amount) {
                    Transaction txn = new Transaction(srcStr, destStr, amount, TransactionType.TRANSFER, "Failed Transfer: Insufficient Funds");
                    txn.setStatus(TransactionStatus.FAILED);
                    transactionRepository.saveTransaction(conn, txn);
                    conn.commit();
                    return BankingTransactionResult.failure(txn, String.format("Insufficient funds in source account %s (Balance: ₹%.2f, Requested: ₹%.2f)", srcStr, srcAcc.getBalance(), amount));
                }

                // Execute Atomic Transfer
                double newSrcBalance = srcAcc.getBalance() - amount;
                double newDestBalance = destAcc.getBalance() + amount;

                accountRepository.updateBalance(conn, srcStr, newSrcBalance);
                accountRepository.updateBalance(conn, destStr, newDestBalance);

                Transaction txn = new Transaction(srcStr, destStr, amount, TransactionType.TRANSFER, "Account Transfer");
                txn.setStatus(TransactionStatus.SUCCESS);
                transactionRepository.saveTransaction(conn, txn);

                conn.commit();
                LoggerUtil.info(String.format("Transfer SUCCESS: ₹%.2f from %s to %s [TxnID: %s]", amount, srcStr, destStr, txn.getTransactionId()));
                return BankingTransactionResult.success(txn, "Transfer completed successfully.");

            } catch (Exception e) {
                conn.rollback();
                LoggerUtil.error("Transfer transaction rolled back due to error: " + e.getMessage());
                return BankingTransactionResult.failure("Transfer transaction rolled back: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Database connection error: " + e.getMessage());
            return BankingTransactionResult.failure("Database error: " + e.getMessage());
        }
    }
}
