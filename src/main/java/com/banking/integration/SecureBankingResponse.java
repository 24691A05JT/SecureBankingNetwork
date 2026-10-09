package com.banking.integration;

/**
 * Model class encapsulating the summary response returned after pipeline execution.
 */
public class SecureBankingResponse {

    private final String transactionId;
    private final String status;
    private final String message;
    private final Double initialSourceBalance;
    private final Double finalSourceBalance;
    private final Double initialDestinationBalance;
    private final Double finalDestinationBalance;

    public SecureBankingResponse(String transactionId, String status, String message,
                                 Double initialSourceBalance, Double finalSourceBalance,
                                 Double initialDestinationBalance, Double finalDestinationBalance) {
        this.transactionId = transactionId != null ? transactionId : "";
        this.status = status != null ? status : "";
        this.message = message != null ? message : "";
        this.initialSourceBalance = initialSourceBalance;
        this.finalSourceBalance = finalSourceBalance;
        this.initialDestinationBalance = initialDestinationBalance;
        this.finalDestinationBalance = finalDestinationBalance;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
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
        return String.format("SecureBankingResponse[TxnID: %s, Status: %s, Message: '%s']",
                transactionId, status, message);
    }
}
