package com.prakash.prakashmart.listener;

import com.prakash.prakashmart.util.DatabaseMigrationRunner;
import com.prakash.prakashmart.util.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.InputStream;
import java.util.Properties;

/**
 * ServletContextListener responsible for initializing and terminating the HikariCP
 * connection pool and executing database schema migrations at application startup and shutdown.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);
    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing PrakashMart Application Context and HikariCP connection pool...");
        try {
            Properties props = new Properties();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                if (in != null) {
                    props.load(in);
                } else {
                    logger.warn("config.properties not found on classpath, checking environment defaults");
                }
            }

            String jdbcUrl = System.getenv("JDBC_URL");
            if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
                jdbcUrl = props.getProperty("db.url", "jdbc:h2:mem:prakashmart;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            }

            String jdbcUser = System.getenv("JDBC_USER");
            if (jdbcUser == null || jdbcUser.trim().isEmpty()) {
                jdbcUser = props.getProperty("db.user", "sa");
            }

            String jdbcPassword = System.getenv("JDBC_PASSWORD");
            if (jdbcPassword == null) {
                jdbcPassword = props.getProperty("db.password", "");
            }

            String driverClassName = props.getProperty("db.driver", "org.h2.Driver");

            HikariConfig config = new HikariConfig();
            config.setDriverClassName(driverClassName);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(jdbcUser);
            config.setPassword(jdbcPassword);
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "300000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "20000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
            config.setPoolName("PrakashMartHikariPool");

            dataSource = new HikariDataSource(config);
            DatabaseUtil.setDataSource(dataSource);
            sce.getServletContext().setAttribute("dataSource", dataSource);

            logger.info("HikariCP connection pool initialized successfully with URL: {}", jdbcUrl);

            // Execute versioned schema migrations
            DatabaseMigrationRunner migrationRunner = new DatabaseMigrationRunner(dataSource);
            migrationRunner.runMigrations();

        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP connection pool or run database migrations", e);
            throw new RuntimeException("Application startup failed due to database pool/migration error", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Closing PrakashMart HikariCP connection pool...");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP connection pool closed.");
        }
    }
}
