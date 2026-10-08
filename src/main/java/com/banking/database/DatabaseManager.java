package com.banking.database;

import com.banking.util.LoggerUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite JDBC Connections, Directory verification, Schema Initializations, and Demo Data Seeding.
 * Supports file-based and in-memory SQLite instances.
 */
public class DatabaseManager implements AutoCloseable {

    public static final String DEFAULT_DB_URL = "jdbc:sqlite:data/banking.db";
    private final String dbUrl;
    private Connection keepAliveConnection; // Holds shared in-memory SQLite database open

    public DatabaseManager() {
        this(DEFAULT_DB_URL);
    }

    public DatabaseManager(String dbUrl) {
        if ("jdbc:sqlite::memory:".equals(dbUrl)) {
            // Convert unshared memory URL to a unique shared in-memory database URI
            this.dbUrl = "jdbc:sqlite:file:mem_" + System.identityHashCode(this) + "?mode=memory&cache=shared";
        } else {
            this.dbUrl = dbUrl != null ? dbUrl : DEFAULT_DB_URL;
        }
        ensureDataDirectoryExists();
        initKeepAliveIfNeeded();
    }

    private void ensureDataDirectoryExists() {
        if (dbUrl.startsWith("jdbc:sqlite:data/")) {
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
        }
    }

    private void initKeepAliveIfNeeded() {
        if (dbUrl.contains("mode=memory") || dbUrl.contains(":memory:")) {
            try {
                this.keepAliveConnection = DriverManager.getConnection(dbUrl);
            } catch (SQLException e) {
                LoggerUtil.error("Failed to establish keep-alive connection for in-memory SQLite: " + e.getMessage());
            }
        }
    }

    /**
     * Obtains a new JDBC connection to the SQLite database.
     *
     * @return Connection instance
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public String getDbUrl() {
        return dbUrl;
    }

    /**
     * Initializes the database schema (creates users, accounts, and transactions tables if they do not exist).
     */
    public void initializeSchema() {
        try (Connection conn = getConnection()) {
            initializeSchema(conn);
        } catch (SQLException e) {
            LoggerUtil.error("Failed to initialize database schema: " + e.getMessage());
            throw new RuntimeException("Database initialization error", e);
        }
    }

    /**
     * Initializes database tables on an existing connection.
     */
    public void initializeSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            // Enable foreign key support in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Users Table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id TEXT UNIQUE NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT NOT NULL,
                    created_at TEXT NOT NULL
                );
            """;
            stmt.execute(createUsersTable);

            // 2. Accounts Table
            String createAccountsTable = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    account_number TEXT UNIQUE NOT NULL,
                    user_id TEXT NOT NULL,
                    account_type TEXT NOT NULL,
                    balance REAL NOT NULL,
                    status TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES users(user_id)
                );
            """;
            stmt.execute(createAccountsTable);

            // 3. Transactions Table
            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_id TEXT UNIQUE NOT NULL,
                    source_account TEXT,
                    destination_account TEXT,
                    amount REAL NOT NULL,
                    transaction_type TEXT NOT NULL,
                    status TEXT NOT NULL,
                    timestamp TEXT NOT NULL,
                    description TEXT
                );
            """;
            stmt.execute(createTransactionsTable);
        }
    }

    /**
     * Seeds initial demo users and accounts if they do not already exist.
     */
    public void seedSampleData() {
        UserRepository userRepo = new UserRepository(this);
        AccountRepository accountRepo = new AccountRepository(this);

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (userRepo.findByUserId(conn, "U1001").isEmpty()) {
                    userRepo.createUser(conn, new com.banking.model.User("U1001", "Demo Customer 1", com.banking.model.UserRole.CUSTOMER));
                }
                if (accountRepo.findByAccountNumber(conn, "ACC10001").isEmpty()) {
                    accountRepo.createAccount(conn, new com.banking.model.Account("ACC10001", "U1001", com.banking.model.AccountType.SAVINGS, 50000.0));
                }

                if (userRepo.findByUserId(conn, "U1002").isEmpty()) {
                    userRepo.createUser(conn, new com.banking.model.User("U1002", "Demo Customer 2", com.banking.model.UserRole.CUSTOMER));
                }
                if (accountRepo.findByAccountNumber(conn, "ACC10002").isEmpty()) {
                    accountRepo.createAccount(conn, new com.banking.model.Account("ACC10002", "U1002", com.banking.model.AccountType.CURRENT, 25000.0));
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                LoggerUtil.error("Failed to seed sample data: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Database connection error during seeding: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (keepAliveConnection != null) {
            try {
                keepAliveConnection.close();
            } catch (SQLException e) {
                // Ignore on shutdown
            }
        }
    }
}
