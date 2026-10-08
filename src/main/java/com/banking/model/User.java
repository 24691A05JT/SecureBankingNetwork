package com.banking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Model class representing a banking user / customer.
 */
public class User {

    private long id;
    private String userId;
    private String fullName;
    private UserRole role;
    private String createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public User(long id, String userId, String fullName, UserRole role, String createdAt) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
        this.role = Objects.requireNonNull(role, "User role cannot be null");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now().format(FORMATTER);
    }

    public User(String userId, String fullName, UserRole role) {
        this(0L, userId, fullName, role, LocalDateTime.now().format(FORMATTER));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, userId='%s', name='%s', role=%s]", id, userId, fullName, role);
    }
}
