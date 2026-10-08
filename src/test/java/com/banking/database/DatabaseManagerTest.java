package com.banking.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        // Use SQLite in-memory database for isolated unit testing
        databaseManager = new DatabaseManager("jdbc:sqlite::memory:");
        databaseManager.initializeSchema();
    }

    @Test
    @DisplayName("Test database connection opening")
    void testGetConnection() throws Exception {
        try (Connection conn = databaseManager.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
    }

    @Test
    @DisplayName("Test database schema table creation")
    void testSchemaTablesExist() throws Exception {
        try (Connection conn = databaseManager.getConnection()) {
            var metaData = conn.getMetaData();
            
            try (ResultSet rs = metaData.getTables(null, null, "users", null)) {
                assertTrue(rs.next(), "Table 'users' should exist");
            }
            try (ResultSet rs = metaData.getTables(null, null, "accounts", null)) {
                assertTrue(rs.next(), "Table 'accounts' should exist");
            }
            try (ResultSet rs = metaData.getTables(null, null, "transactions", null)) {
                assertTrue(rs.next(), "Table 'transactions' should exist");
            }
        }
    }
}
