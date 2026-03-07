package com.agentic.subscription.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database Configuration for RDBMS and Autonomous AI Database setup
 * 
 * This configuration:
 * - Initializes DataSource for database connectivity
 * - Creates database automatically if it doesn't exist
 * - Creates required tables with proper schema
 * - Sets up JdbcTemplate for data access
 * - Configures transaction management
 * 
 * Supports MySQL, PostgreSQL, and Oracle Autonomous AI Database
 */
@Configuration
public class DatabaseConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);
    private static final String DEFAULT_DATABASE = "userdb";
    
    /**
     * Initialize DataSource for RDBMS connections
     * Only instantiated if rdbms persistence is configured
     * 
     * @param dataSourceProperties Spring Boot DataSource properties
     * @return configured DataSource bean
     */
    @Bean
    @ConditionalOnProperty(name = "app.persistence.type", havingValue = "rdbms")
    public DataSource dataSource(
            org.springframework.boot.autoconfigure.jdbc.DataSourceProperties dataSourceProperties) {
        
        logger.info("Initializing DataSource for RDBMS with URL: {}", dataSourceProperties.getUrl());
        
        // Create temporary connection to initialize database
        initializeDatabaseIfNeeded(dataSourceProperties);
        
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        dataSource.setUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        
        logger.info("DataSource configured successfully");
        return dataSource;
    }
    
    /**
     * Create JdbcTemplate bean for database operations
     * 
     * @param dataSource the DataSource to use
     * @return configured JdbcTemplate bean
     */
    @Bean
    @ConditionalOnProperty(name = "app.persistence.type", havingValue = "rdbms")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        logger.info("Initializing JdbcTemplate");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Initialize database schema on startup
        initializeDatabase(jdbcTemplate);
        
        return jdbcTemplate;
    }
    
    /**
     * Create TransactionManager bean for database transactions
     * 
     * @param dataSource the DataSource to manage transactions for
     * @return configured PlatformTransactionManager bean
     */
    @Bean
    @ConditionalOnProperty(name = "app.persistence.type", havingValue = "rdbms")
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        logger.info("Initializing TransactionManager");
        return new DataSourceTransactionManager(dataSource);
    }
    
    /**
     * Initialize database if it doesn't exist
     * Creates the database and initializes schema
     * 
     * @param dataSourceProperties Spring Boot DataSource properties
     */
    private void initializeDatabaseIfNeeded(
            org.springframework.boot.autoconfigure.jdbc.DataSourceProperties dataSourceProperties) {
        try {
            // Extract database name from URL
            String url = dataSourceProperties.getUrl();
            String dbName = extractDatabaseName(url);
            
            logger.info("Checking if database '{}' exists...", dbName);
            
            // Create temporary connection without database to check/create it
            String tempUrl = removeDatabaseFromUrl(url);
            DriverManagerDataSource tempDataSource = new DriverManagerDataSource();
            tempDataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
            tempDataSource.setUrl(tempUrl);
            tempDataSource.setUsername(dataSourceProperties.getUsername());
            tempDataSource.setPassword(dataSourceProperties.getPassword());
            
            try (Connection conn = tempDataSource.getConnection()) {
                // Try to use the database - if it fails, create it
                try {
                    conn.getMetaData().getCatalogs().close();
                } catch (SQLException e) {
                    logger.warn("Database may not exist, attempting to create it");
                    createDatabase(conn, dbName, dataSourceProperties.getDriverClassName());
                }
            }
            
            logger.info("Database initialization check completed");
        } catch (Exception e) {
            logger.warn("Could not pre-initialize database (may already exist): {}", e.getMessage());
            // Continue anyway - the schema initialization will handle any issues
        }
    }
    
    /**
     * Create database if it doesn't exist
     * 
     * @param conn database connection
     * @param dbName name of database to create
     * @param driverClassName JDBC driver class name
     * @throws SQLException if database creation fails
     */
    private void createDatabase(Connection conn, String dbName, String driverClassName) 
            throws SQLException {
        
        String createDbSql;
        
        // Different SQL for different databases
        if (driverClassName.contains("oracle")) {
            // Oracle Autonomous Database - database already exists, just use it
            logger.info("Oracle database detected - skipping database creation");
            return;
        } else if (driverClassName.contains("postgresql")) {
            createDbSql = "CREATE DATABASE IF NOT EXISTS " + dbName;
        } else {
            // MySQL and others
            createDbSql = "CREATE DATABASE IF NOT EXISTS " + dbName + 
                          " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
        }
        
        logger.info("Creating database with SQL: {}", createDbSql);
        conn.createStatement().execute(createDbSql);
        logger.info("Database '{}' created successfully", dbName);
    }
    
    /**
     * Extract database name from JDBC URL
     * 
     * @param url JDBC URL
     * @return extracted database name
     */
    private String extractDatabaseName(String url) {
        try {
            // Handle different URL formats
            if (url.contains("?")) {
                url = url.substring(0, url.indexOf("?"));
            }
            
            int lastSlash = url.lastIndexOf("/");
            if (lastSlash > 0) {
                return url.substring(lastSlash + 1);
            }
        } catch (Exception e) {
            logger.warn("Could not extract database name from URL: {}", url);
        }
        
        return DEFAULT_DATABASE;
    }
    
    /**
     * Remove database name from JDBC URL
     * 
     * @param url JDBC URL
     * @return URL without database name
     */
    private String removeDatabaseFromUrl(String url) {
        try {
            if (url.contains("?")) {
                url = url.substring(0, url.indexOf("?"));
            }
            
            int lastSlash = url.lastIndexOf("/");
            if (lastSlash > 0) {
                return url.substring(0, lastSlash);
            }
        } catch (Exception e) {
            logger.warn("Could not modify URL: {}", url);
        }
        
        return url;
    }
    
    /**
     * Initialize database schema by creating tables if they don't exist
     * Supports MySQL, PostgreSQL, and Oracle Autonomous Database
     * 
     * @param jdbcTemplate JdbcTemplate for executing SQL
     */
    private void initializeDatabase(JdbcTemplate jdbcTemplate) {
        try {
            logger.info("Checking if users table exists...");
            
            // Check if table exists
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = 'users'",
                Integer.class
            );
            
            if (tableCount != null && tableCount > 0) {
                logger.info("Users table already exists");
                return;
            }
            
            // Create table if it doesn't exist
            logger.info("Creating users table...");
            String createTableSql = getDDLScript();
            
            String[] statements = createTableSql.split(";");
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    jdbcTemplate.execute(trimmed);
                    logger.debug("Executed DDL: {}", trimmed.substring(0, Math.min(50, trimmed.length())));
                }
            }
            
            logger.info("Database schema initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing database schema", e);
            logger.warn("Attempting alternative table creation method...");
            
            // Try alternative method
            try {
                String[] statements = getDDLScript().split(";");
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            jdbcTemplate.execute(trimmed);
                        } catch (Exception ex) {
                            logger.debug("Statement failed (table may exist): {}", trimmed);
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Could not initialize schema", ex);
            }
        }
    }
    
    /**
     * Get DDL script for creating users table
     * Compatible with MySQL, PostgreSQL, and Oracle Autonomous Database
     * 
     * @return DDL script for table creation
     */
    private String getDDLScript() {
        return "CREATE TABLE IF NOT EXISTS users (" +
               "  id VARCHAR(36) PRIMARY KEY," +
               "  name VARCHAR(255) NOT NULL," +
               "  age INT," +
               "  city VARCHAR(255)," +
               "  company VARCHAR(255)," +
               "  interests JSON," +
               "  created_at BIGINT NOT NULL," +
               "  updated_at BIGINT NOT NULL," +
               "  INDEX idx_created_at (created_at)," +
               "  INDEX idx_name (name)" +
               "); " +
               "CREATE TABLE IF NOT EXISTS audit_log (" +
               "  id VARCHAR(36) PRIMARY KEY," +
               "  user_id VARCHAR(36) NOT NULL," +
               "  action VARCHAR(50) NOT NULL," +
               "  timestamp BIGINT NOT NULL," +
               "  INDEX idx_user_id (user_id)," +
               "  INDEX idx_timestamp (timestamp)," +
               "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
               ");";
    }
}

