package com.banking.integration;

/**
 * Immutable container encapsulating the complete result, metrics, and stage status
 * of an end-to-end secure banking pipeline execution.
 */
public final class PipelineResult {

    private final boolean successful;
    private final String transactionId;
    private final String sourceAccount;
    private final String destinationAccount;
    private final double amount;

    private final String routingPath;
    private final double routingCost;
    private final String nextHop;
    private final String distanceVectorPath;
    private final double distanceVectorCost;

    private final boolean encryptionSuccessful;
    private final boolean crcGenerated;
    private final boolean crcVerified;
    private final boolean reliableTransmissionSuccessful;
    private final boolean tcpSuccessful;
    private final boolean decryptionSuccessful;
    private final boolean bankingTransactionSuccessful;
    private final boolean databasePersistenceSuccessful;

    private final String finalResponse;
    private final String errorMessage;

    private final Double initialSourceBalance;
    private final Double finalSourceBalance;
    private final Double initialDestinationBalance;
    private final Double finalDestinationBalance;

    private PipelineResult(Builder builder) {
        this.successful = builder.successful;
        this.transactionId = builder.transactionId;
        this.sourceAccount = builder.sourceAccount;
        this.destinationAccount = builder.destinationAccount;
        this.amount = builder.amount;
        this.routingPath = builder.routingPath;
        this.routingCost = builder.routingCost;
        this.nextHop = builder.nextHop;
        this.distanceVectorPath = builder.distanceVectorPath;
        this.distanceVectorCost = builder.distanceVectorCost;
        this.encryptionSuccessful = builder.encryptionSuccessful;
        this.crcGenerated = builder.crcGenerated;
        this.crcVerified = builder.crcVerified;
        this.reliableTransmissionSuccessful = builder.reliableTransmissionSuccessful;
        this.tcpSuccessful = builder.tcpSuccessful;
        this.decryptionSuccessful = builder.decryptionSuccessful;
        this.bankingTransactionSuccessful = builder.bankingTransactionSuccessful;
        this.databasePersistenceSuccessful = builder.databasePersistenceSuccessful;
        this.finalResponse = builder.finalResponse;
        this.errorMessage = builder.errorMessage;
        this.initialSourceBalance = builder.initialSourceBalance;
        this.finalSourceBalance = builder.finalSourceBalance;
        this.initialDestinationBalance = builder.initialDestinationBalance;
        this.finalDestinationBalance = builder.finalDestinationBalance;
    }

    public boolean isSuccessful() {
        return successful;
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

    public String getRoutingPath() {
        return routingPath;
    }

    public double getRoutingCost() {
        return routingCost;
    }

    public String getNextHop() {
        return nextHop;
    }

    public String getDistanceVectorPath() {
        return distanceVectorPath;
    }

    public double getDistanceVectorCost() {
        return distanceVectorCost;
    }

    public boolean isEncryptionSuccessful() {
        return encryptionSuccessful;
    }

    public boolean isCrcGenerated() {
        return crcGenerated;
    }

    public boolean isCrcVerified() {
        return crcVerified;
    }

    public boolean isReliableTransmissionSuccessful() {
        return reliableTransmissionSuccessful;
    }

    public boolean isTcpSuccessful() {
        return tcpSuccessful;
    }

    public boolean isDecryptionSuccessful() {
        return decryptionSuccessful;
    }

    public boolean isBankingTransactionSuccessful() {
        return bankingTransactionSuccessful;
    }

    public boolean isDatabasePersistenceSuccessful() {
        return databasePersistenceSuccessful;
    }

    public String getFinalResponse() {
        return finalResponse;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Double getInitialSourceBalance() {
        return initialSourceBalance;
    }

    public Double getFinalSourceBalance() {
        return finalSourceBalance;
    }

    public Double getInitialDestinationBalance() {
        return initialDestinationBalance;
    }

    public Double getFinalDestinationBalance() {
        return finalDestinationBalance;
    }

    @Override
    public String toString() {
        return String.format("PipelineResult[Status: %s, TxnID: %s, Path: %s, Cost: %.1f, SafeBalanceSrc: %s->%s]",
                successful ? "SUCCESS" : "FAILED",
                transactionId,
                routingPath,
                routingCost,
                initialSourceBalance,
                finalSourceBalance);
    }

    public static class Builder {
        private boolean successful;
        private String transactionId = "";
        private String sourceAccount = "";
        private String destinationAccount = "";
        private double amount;
        private String routingPath = "";
        private double routingCost;
        private String nextHop = "";
        private String distanceVectorPath = "";
        private double distanceVectorCost;

        private boolean encryptionSuccessful;
        private boolean crcGenerated;
        private boolean crcVerified;
        private boolean reliableTransmissionSuccessful;
        private boolean tcpSuccessful;
        private boolean decryptionSuccessful;
        private boolean bankingTransactionSuccessful;
        private boolean databasePersistenceSuccessful;

        private String finalResponse = "";
        private String errorMessage = "";

        private Double initialSourceBalance;
        private Double finalSourceBalance;
        private Double initialDestinationBalance;
        private Double finalDestinationBalance;

        public Builder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder sourceAccount(String sourceAccount) {
            this.sourceAccount = sourceAccount;
            return this;
        }

        public Builder destinationAccount(String destinationAccount) {
            this.destinationAccount = destinationAccount;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder routingPath(String routingPath) {
            this.routingPath = routingPath;
            return this;
        }

        public Builder routingCost(double routingCost) {
            this.routingCost = routingCost;
            return this;
        }

        public Builder nextHop(String nextHop) {
            this.nextHop = nextHop;
            return this;
        }

        public Builder distanceVectorPath(String distanceVectorPath) {
            this.distanceVectorPath = distanceVectorPath;
            return this;
        }

        public Builder distanceVectorCost(double distanceVectorCost) {
            this.distanceVectorCost = distanceVectorCost;
            return this;
        }

        public Builder encryptionSuccessful(boolean encryptionSuccessful) {
            this.encryptionSuccessful = encryptionSuccessful;
            return this;
        }

        public Builder crcGenerated(boolean crcGenerated) {
            this.crcGenerated = crcGenerated;
            return this;
        }

        public Builder crcVerified(boolean crcVerified) {
            this.crcVerified = crcVerified;
            return this;
        }

        public Builder reliableTransmissionSuccessful(boolean reliableTransmissionSuccessful) {
            this.reliableTransmissionSuccessful = reliableTransmissionSuccessful;
            return this;
        }

        public Builder tcpSuccessful(boolean tcpSuccessful) {
            this.tcpSuccessful = tcpSuccessful;
            return this;
        }

        public Builder decryptionSuccessful(boolean decryptionSuccessful) {
            this.decryptionSuccessful = decryptionSuccessful;
            return this;
        }

        public Builder bankingTransactionSuccessful(boolean bankingTransactionSuccessful) {
            this.bankingTransactionSuccessful = bankingTransactionSuccessful;
            return this;
        }

        public Builder databasePersistenceSuccessful(boolean databasePersistenceSuccessful) {
            this.databasePersistenceSuccessful = databasePersistenceSuccessful;
            return this;
        }

        public Builder finalResponse(String finalResponse) {
            this.finalResponse = finalResponse;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder initialSourceBalance(Double initialSourceBalance) {
            this.initialSourceBalance = initialSourceBalance;
            return this;
        }

        public Builder finalSourceBalance(Double finalSourceBalance) {
            this.finalSourceBalance = finalSourceBalance;
            return this;
        }

        public Builder initialDestinationBalance(Double initialDestinationBalance) {
            this.initialDestinationBalance = initialDestinationBalance;
            return this;
        }

        public Builder finalDestinationBalance(Double finalDestinationBalance) {
            this.finalDestinationBalance = finalDestinationBalance;
            return this;
        }

        public PipelineResult build() {
            return new PipelineResult(this);
        }
    }
}
