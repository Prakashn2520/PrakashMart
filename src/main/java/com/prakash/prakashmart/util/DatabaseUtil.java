package com.prakash.prakashmart.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility for providing database connections from the application connection pool.
 * Lifecycle is owned and managed by AppContextListener.
 */
public final class DatabaseUtil {

    private static volatile DataSource dataSource;

    private DatabaseUtil() {
        // Prevent instantiation
    }

    public static void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Obtains a connection from the configured connection pool.
     * Callers must wrap the connection in a try-with-resources block.
     *
     * @return an open SQL Connection
     * @throws SQLException if a database access error occurs or pool is not initialized
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource has not been initialized by AppContextListener");
        }
        return dataSource.getConnection();
    }
}
