package com.banking.database;

import com.banking.model.User;
import com.banking.model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Repository for User entities.
 */
public class UserRepository {

    private final DatabaseManager databaseManager;

    public UserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void createUser(User user) {
        try (Connection conn = databaseManager.getConnection()) {
            createUser(conn, user);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public void createUser(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO users (user_id, full_name, role, created_at) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getRole().name());
            pstmt.setString(4, user.getCreatedAt());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
            }
        }
    }

    public Optional<User> findByUserId(String userId) {
        try (Connection conn = databaseManager.getConnection()) {
            return findByUserId(conn, userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by ID: " + e.getMessage(), e);
        }
    }

    public Optional<User> findByUserId(Connection conn, String userId) throws SQLException {
        String sql = "SELECT id, user_id, full_name, role, created_at FROM users WHERE user_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, user_id, full_name, role, created_at FROM users ORDER BY id ASC;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all users: " + e.getMessage(), e);
        }
        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("user_id"),
                rs.getString("full_name"),
                UserRole.valueOf(rs.getString("role")),
                rs.getString("created_at")
        );
    }
}
