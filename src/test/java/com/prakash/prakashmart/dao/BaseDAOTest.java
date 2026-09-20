package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.util.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Base class for DAO tests initializing embedded in-memory H2 database
 * and executing schema.sql.
 */
public abstract class BaseDAOTest {

    private static HikariDataSource testDataSource;

    @BeforeAll
    static void initDatabase() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);
        config.setPoolName("TestHikariPool");

        testDataSource = new HikariDataSource(config);
        DatabaseUtil.setDataSource(testDataSource);
    }

    @BeforeEach
    void resetSchema() throws Exception {
        Path schemaPath = Paths.get("db", "schema.sql");
        String sql;
        if (Files.exists(schemaPath)) {
            sql = Files.readString(schemaPath, StandardCharsets.UTF_8);
        } else {
            // Fallback to reading from classpath if run from different cwd
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
                if (in == null) {
                    throw new IllegalStateException("schema.sql not found");
                }
                sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            org.h2.tools.RunScript.execute(conn, new java.io.StringReader(sql));
        }
    }

    @AfterAll
    static void tearDownDatabase() {
        if (testDataSource != null && !testDataSource.isClosed()) {
            testDataSource.close();
        }
    }
}
