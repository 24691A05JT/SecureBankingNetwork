package com.banking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Model class representing a Bank Account.
 */
public class Account {

    private long id;
    private String accountNumber;
    private String userId;
    private AccountType accountType;
    private double balance;
    private AccountStatus status;
    private String createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Account(long id, String accountNumber, String userId, AccountType accountType, double balance, AccountStatus status, String createdAt) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty or null.");
        }
        if (balance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative, got: " + balance);
        }
        this.id = id;
        this.accountNumber = accountNumber.trim();
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.accountType = Objects.requireNonNull(accountType, "Account type cannot be null");
        this.balance = balance;
        this.status = Objects.requireNonNull(status, "Account status cannot be null");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now().format(FORMATTER);
    }

    public Account(String accountNumber, String userId, AccountType accountType, double balance) {
        this(0L, accountNumber, userId, accountType, balance, AccountStatus.ACTIVE, LocalDateTime.now().format(FORMATTER));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUserId() {
        return userId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Account balance cannot be negative: " + balance);
        }
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public String toString() {
        return String.format("Account[number='%s', user='%s', type=%s, balance=₹%.2f, status=%s]",
                accountNumber, userId, accountType, balance, status);
    }
}
