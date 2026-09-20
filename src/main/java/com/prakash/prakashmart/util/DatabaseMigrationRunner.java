package com.prakash.prakashmart.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lightweight, zero-dependency Database Migration Runner for PrakashMart.
 * Automatically discovers versioned SQL migrations (e.g. V1__init_schema.sql, V2__initial_seed.sql)
 * and applies them in sequential order, tracking applied migrations in `schema_migrations`.
 */
public class DatabaseMigrationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    private final DataSource dataSource;

    public DatabaseMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Executes all pending database migrations.
     */
    public void runMigrations() {
        logger.info("Starting database migration check...");
        try (Connection conn = dataSource.getConnection()) {
            ensureMigrationTableExists(conn);
            List<String> appliedMigrations = getAppliedMigrations(conn);
            List<String> migrationFiles = discoverMigrationFiles();

            Collections.sort(migrationFiles);

            for (String migrationFile : migrationFiles) {
                String migrationName = extractMigrationName(migrationFile);
                if (!appliedMigrations.contains(migrationName)) {
                    logger.info("Applying migration: {}", migrationName);
                    String sql = loadMigrationSql(migrationFile);
                    applyMigration(conn, migrationName, sql);
                    logger.info("Successfully applied migration: {}", migrationName);
                } else {
                    logger.debug("Migration {} already applied, skipping.", migrationName);
                }
            }
            logger.info("Database migration check completed.");
        } catch (Exception e) {
            logger.error("Failed to run database migrations", e);
            throw new RuntimeException("Database migration failed", e);
        }
    }

    private void ensureMigrationTableExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS schema_migrations ("
                   + "version VARCHAR(100) PRIMARY KEY, "
                   + "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL"
                   + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private List<String> getAppliedMigrations(Connection conn) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT version FROM schema_migrations ORDER BY version ASC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("version"));
            }
        }
        return list;
    }

    private List<String> discoverMigrationFiles() {
        List<String> list = new ArrayList<>();
        // 1. Try file system relative path
        Path migrationsDir = Paths.get("db", "migrations");
        if (Files.isDirectory(migrationsDir)) {
            try (Stream<Path> stream = Files.list(migrationsDir)) {
                return stream
                        .map(p -> p.getFileName().toString())
                        .filter(name -> name.startsWith("V") && name.endsWith(".sql"))
                        .sorted()
                        .collect(Collectors.toList());
            } catch (Exception e) {
                logger.warn("Could not list migrations from directory: {}", migrationsDir, e);
            }
        }

        // 2. Known default migrations fallback
        list.add("V1__init_schema.sql");
        list.add("V2__initial_seed.sql");
        return list;
    }

    private String extractMigrationName(String filename) {
        if (filename.contains(File.separator)) {
            filename = filename.substring(filename.lastIndexOf(File.separator) + 1);
        }
        if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf('/') + 1);
        }
        if (filename.endsWith(".sql")) {
            return filename.substring(0, filename.length() - 4);
        }
        return filename;
    }

    private String loadMigrationSql(String filename) {
        // Try file system first
        Path path = Paths.get("db", "migrations", filename);
        if (Files.exists(path)) {
            try {
                return Files.readString(path, StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.warn("Could not read migration from path: {}", path, e);
            }
        }

        // Try classpath db/migrations/
        String resourcePath = "db/migrations/" + filename;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            logger.warn("Could not read migration from classpath resource: {}", resourcePath, e);
        }

        // Fallback to classpath schema.sql for V1
        if (filename.startsWith("V1")) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
                if (in != null) {
                    return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));
                }
            } catch (Exception e) {
                logger.error("Could not load schema.sql fallback", e);
            }
        }

        throw new IllegalStateException("Migration script not found: " + filename);
    }

    private void applyMigration(Connection conn, String migrationName, String sql) throws SQLException {
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            org.h2.tools.RunScript.execute(conn, new java.io.StringReader(sql));

            String recordSql = "INSERT INTO schema_migrations (version, applied_at) VALUES (?, CURRENT_TIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(recordSql)) {
                ps.setString(1, migrationName);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw new SQLException("Failed to execute migration " + migrationName, e);
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }
}
