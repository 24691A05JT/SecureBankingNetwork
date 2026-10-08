package com.banking.database;

import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Repository for Transaction entities.
 */
public class TransactionRepository {

    private final DatabaseManager databaseManager;

    public TransactionRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void saveTransaction(Transaction transaction) {
        try (Connection conn = databaseManager.getConnection()) {
            saveTransaction(conn, transaction);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction: " + e.getMessage(), e);
        }
    }

    public void saveTransaction(Connection conn, Transaction transaction) throws SQLException {
        String sql = """
            INSERT INTO transactions (transaction_id, source_account, destination_account, amount, transaction_type, status, timestamp, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, transaction.getTransactionId());
            pstmt.setString(2, transaction.getSourceAccount());
            pstmt.setString(3, transaction.getDestinationAccount());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setString(5, transaction.getTransactionType().name());
            pstmt.setString(6, transaction.getStatus().name());
            pstmt.setString(7, transaction.getTimestamp());
            pstmt.setString(8, transaction.getDescription());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    transaction.setId(rs.getLong(1));
                }
            }
        }
    }

    public Optional<Transaction> findTransactionById(String transactionId) {
        try (Connection conn = databaseManager.getConnection()) {
            return findTransactionById(conn, transactionId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transaction: " + e.getMessage(), e);
        }
    }

    public Optional<Transaction> findTransactionById(Connection conn, String transactionId) throws SQLException {
        String sql = "SELECT id, transaction_id, source_account, destination_account, amount, transaction_type, status, timestamp, description FROM transactions WHERE transaction_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Transaction> findAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, transaction_id, source_account, destination_account, amount, transaction_type, status, timestamp, description FROM transactions ORDER BY id DESC;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch transactions: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Transaction> findTransactionsByAccount(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, transaction_id, source_account, destination_account, amount, transaction_type, status, timestamp, description FROM transactions WHERE source_account = ? OR destination_account = ? ORDER BY id DESC;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch account transactions: " + e.getMessage(), e);
        }
        return list;
    }

    public void updateTransactionStatus(String transactionId, TransactionStatus status) {
        try (Connection conn = databaseManager.getConnection()) {
            updateTransactionStatus(conn, transactionId, status);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction status: " + e.getMessage(), e);
        }
    }

    public void updateTransactionStatus(Connection conn, String transactionId, TransactionStatus status) throws SQLException {
        String sql = "UPDATE transactions SET status = ? WHERE transaction_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, transactionId);
            pstmt.executeUpdate();
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getLong("id"),
                rs.getString("transaction_id"),
                rs.getString("source_account"),
                rs.getString("destination_account"),
                rs.getDouble("amount"),
                TransactionType.valueOf(rs.getString("transaction_type")),
                TransactionStatus.valueOf(rs.getString("status")),
                rs.getString("timestamp"),
                rs.getString("description")
        );
    }
}
