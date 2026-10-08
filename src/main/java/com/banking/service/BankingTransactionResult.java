package com.banking.service;

import com.banking.model.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Result DTO representing the outcome of a banking transaction operation.
 */
public final class BankingTransactionResult {

    private final boolean success;
    private final String transactionId;
    private final String message;
    private final String sourceAccount;
    private final String destinationAccount;
    private final double amount;
    private final String timestamp;
    private final Transaction transaction;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BankingTransactionResult(boolean success, String transactionId, String message,
                                    String sourceAccount, String destinationAccount,
                                    double amount, String timestamp, Transaction transaction) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now().format(FORMATTER);
        this.transaction = transaction;
    }

    public static BankingTransactionResult success(Transaction transaction, String message) {
        return new BankingTransactionResult(
                true,
                transaction.getTransactionId(),
                message,
                transaction.getSourceAccount(),
                transaction.getDestinationAccount(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                transaction
        );
    }

    public static BankingTransactionResult failure(String message) {
        return new BankingTransactionResult(
                false,
                null,
                message,
                null,
                null,
                0.0,
                LocalDateTime.now().format(FORMATTER),
                null
        );
    }

    public static BankingTransactionResult failure(Transaction transaction, String message) {
        return new BankingTransactionResult(
                false,
                transaction != null ? transaction.getTransactionId() : null,
                message,
                transaction != null ? transaction.getSourceAccount() : null,
                transaction != null ? transaction.getDestinationAccount() : null,
                transaction != null ? transaction.getAmount() : 0.0,
                transaction != null ? transaction.getTimestamp() : LocalDateTime.now().format(FORMATTER),
                transaction
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMessage() {
        return message;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public double getAmount() {
        return amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public String toString() {
        return String.format("BankingTransactionResult[success=%b, id='%s', msg='%s', amount=₹%.2f]",
                success, transactionId, message, amount);
    }
}
