package com.banking.integration;

import com.banking.model.TransactionType;

/**
 * Model class representing a request to transmit and process a secure banking transaction
 * through the integrated pipeline.
 */
public class SecureBankingRequest {

    private final String sourceAccount;
    private final String destinationAccount;
    private final double amount;
    private final TransactionType transactionType;
    private final String description;
    private final String sourceNodeId;
    private final String destinationNodeId;

    public SecureBankingRequest(String sourceAccount, String destinationAccount, double amount) {
        this(sourceAccount, destinationAccount, amount, TransactionType.TRANSFER, "Secure Day 11 Integrated Transfer", "NODE-B1", "NODE-DB");
    }

    public SecureBankingRequest(String sourceAccount, String destinationAccount, double amount, TransactionType transactionType, String description) {
        this(sourceAccount, destinationAccount, amount, transactionType, description, "NODE-B1", "NODE-DB");
    }

    public SecureBankingRequest(String sourceAccount, String destinationAccount, double amount,
                                TransactionType transactionType, String description,
                                String sourceNodeId, String destinationNodeId) {
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.transactionType = transactionType != null ? transactionType : TransactionType.TRANSFER;
        this.description = description != null ? description : "Secure Integrated Transfer";
        this.sourceNodeId = sourceNodeId != null ? sourceNodeId : "NODE-B1";
        this.destinationNodeId = destinationNodeId != null ? destinationNodeId : "NODE-DB";
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

    public String getDescription() {
        return description;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public String getDestinationNodeId() {
        return destinationNodeId;
    }

    @Override
    public String toString() {
        return String.format("SecureBankingRequest[Src: %s, Dest: %s, Amount: ₹%.2f, Type: %s, Path: %s->%s]",
                sourceAccount, destinationAccount, amount, transactionType, sourceNodeId, destinationNodeId);
    }
}
