package com.banking.database;

import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Repository for Account entities.
 */
public class AccountRepository {

    private final DatabaseManager databaseManager;

    public AccountRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void createAccount(Account account) {
        try (Connection conn = databaseManager.getConnection()) {
            createAccount(conn, account);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create account: " + e.getMessage(), e);
        }
    }

    public void createAccount(Connection conn, Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_number, user_id, account_type, balance, status, created_at) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, account.getAccountNumber());
            pstmt.setString(2, account.getUserId());
            pstmt.setString(3, account.getAccountType().name());
            pstmt.setDouble(4, account.getBalance());
            pstmt.setString(5, account.getStatus().name());
            pstmt.setString(6, account.getCreatedAt());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    account.setId(rs.getLong(1));
                }
            }
        }
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        try (Connection conn = databaseManager.getConnection()) {
            return findByAccountNumber(conn, accountNumber);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account: " + e.getMessage(), e);
        }
    }

    public Optional<Account> findByAccountNumber(Connection conn, String accountNumber) throws SQLException {
        String sql = "SELECT id, account_number, user_id, account_type, balance, status, created_at FROM accounts WHERE account_number = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void updateBalance(String accountNumber, double newBalance) {
        try (Connection conn = databaseManager.getConnection()) {
            updateBalance(conn, accountNumber, newBalance);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update balance: " + e.getMessage(), e);
        }
    }

    public void updateBalance(Connection conn, String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountNumber);
            pstmt.executeUpdate();
        }
    }

    public void updateStatus(String accountNumber, AccountStatus status) {
        String sql = "UPDATE accounts SET status = ? WHERE account_number = ?;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, accountNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account status: " + e.getMessage(), e);
        }
    }

    public List<Account> findAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT id, account_number, user_id, account_type, balance, status, created_at FROM accounts ORDER BY id ASC;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all accounts: " + e.getMessage(), e);
        }
        return accounts;
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getLong("id"),
                rs.getString("account_number"),
                rs.getString("user_id"),
                AccountType.valueOf(rs.getString("account_type")),
                rs.getDouble("balance"),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getString("created_at")
        );
    }
}
