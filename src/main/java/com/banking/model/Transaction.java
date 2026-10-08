package com.banking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Model class representing a financial banking transaction.
 */
public class Transaction {

    private long id;
    private String transactionId;
    private String sourceAccount;
    private String destinationAccount;
    private double amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String timestamp;
    private String description;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Transaction(long id, String transactionId, String sourceAccount, String destinationAccount,
                       double amount, TransactionType transactionType, TransactionStatus status,
                       String timestamp, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero, got: " + amount);
        }
        this.id = id;
        this.transactionId = transactionId != null ? transactionId : "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.transactionType = Objects.requireNonNull(transactionType, "Transaction type cannot be null");
        this.status = Objects.requireNonNull(status, "Transaction status cannot be null");
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now().format(FORMATTER);
        this.description = description != null ? description : "";
    }

    public Transaction(String sourceAccount, String destinationAccount, double amount, TransactionType transactionType, String description) {
        this(0L, null, sourceAccount, destinationAccount, amount, transactionType, TransactionStatus.PENDING, null, description);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = Objects.requireNonNull(status, "Transaction status cannot be null");
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return String.format("Transaction[id='%s', src='%s', dest='%s', amount=₹%.2f, type=%s, status=%s]",
                transactionId, sourceAccount, destinationAccount, amount, transactionType, status);
    }
}
