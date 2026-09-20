package com.prakash.prakashmart.util;

import com.prakash.prakashmart.dao.BaseDAOTest;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseMigrationRunnerTest extends BaseDAOTest {

    @Test
    @DisplayName("Should execute migrations and record them in schema_migrations table")
    void testRunMigrations() throws Exception {
        // Drop existing tables to test migration runner from scratch
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }

        DatabaseMigrationRunner runner = new DatabaseMigrationRunner(DatabaseUtil.getDataSource());
        runner.runMigrations();

        // Check schema_migrations table
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM schema_migrations")) {
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) >= 2, "Should have executed at least 2 migrations (V1, V2)");
        }

        // Running it a second time should be idempotent
        assertDoesNotThrow(() -> runner.runMigrations());
    }
}
