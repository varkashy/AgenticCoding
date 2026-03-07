package com.agentic.subscription.dao;

import com.agentic.subscription.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * RDBMS Data Access Object Implementation for User Management
 * 
 * This implementation uses Spring's JdbcTemplate for database operations.
 * It supports multiple RDBMS systems including MySQL, PostgreSQL, Oracle, and others.
 * 
 * Features:
 * - CRUD (Create, Read, Update, Delete) operations for users
 * - JSON handling for user interests stored as arrays
 * - Automatic ID and timestamp generation
 * - Proper error handling and logging
 * - Row mapping for ResultSet to User object conversion
 * 
 * SQL Support:
 * - MySQL: Uses JSON data type for interests storage
 * - PostgreSQL: Compatible with JSON operations
 * - Oracle: Compatible with CLOB or VARCHAR2 for JSON
 */
@Repository("rdbmsUserDAO")
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "rdbms")
public class RdbmsUserDAO implements UserDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(RdbmsUserDAO.class);
    private static final String TABLE_NAME = "users";
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Constructor for dependency injection
     * 
     * @param jdbcTemplate the Spring JdbcTemplate for database operations
     */
    @Autowired
    public RdbmsUserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        logger.info("RDBMS DAO initialized with JdbcTemplate");
    }
    
    /**
     * Row mapper for converting a SQL ResultSet into a User object
     * 
     * Handles the conversion of database columns to User properties:
     * - String and non-string types
     * - JSON parsing for interests array
     * - Timestamp handling
     */
    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        /**
         * Map a row from the ResultSet to a User object
         * 
         * @param rs the ResultSet containing the row data
         * @param rowNum the row number (0-indexed)
         * @return a populated User object
         * @throws SQLException if database access fails
         */
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setAge(rs.getInt("age"));
            user.setCity(rs.getString("city"));
            user.setCompany(rs.getString("company"));
            
            String interestsJson = rs.getString("interests");
            if (interestsJson != null && !interestsJson.isEmpty()) {
                user.setInterests(parseInterests(interestsJson));
            }
            
            user.setCreatedAt(rs.getLong("created_at"));
            user.setUpdatedAt(rs.getLong("updated_at"));
            return user;
        }
    };
    
    /**
     * Create a new user in the database
     * 
     * This method:
     * 1. Generates a UUID if not provided
     * 2. Sets creation and update timestamps
     * 3. Converts interests list to JSON format
     * 4. Inserts the user record into the database
     * 
     * @param user the User object to create (can have ID null for auto-generation)
     * @return the created User object with generated ID and timestamps
     * @throws RuntimeException if database operation fails
     */
    @Override
    public User create(User user) {
        try {
            if (user.getId() == null) {
                user.setId(UUID.randomUUID().toString());
            }
            long currentTime = System.currentTimeMillis();
            user.setCreatedAt(currentTime);
            user.setUpdatedAt(currentTime);
            
            String interestsJson = convertInterestsToJson(user.getInterests());
            
            String sql = "INSERT INTO " + TABLE_NAME + 
                         " (id, name, age, city, company, interests, created_at, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            jdbcTemplate.update(sql, 
                user.getId(),
                user.getName(),
                user.getAge(),
                user.getCity(),
                user.getCompany(),
                interestsJson,
                user.getCreatedAt(),
                user.getUpdatedAt()
            );
            
            logger.info("User created successfully: {}", user.getId());
            return user;
        } catch (Exception e) {
            logger.error("Error creating user in RDBMS", e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    /**
     * Retrieve a user by their unique identifier
     * 
     * @param id the unique user ID (UUID format)
     * @return Optional containing the User if found, empty Optional otherwise
     * @throws RuntimeException if database operation fails
     */
    @Override
    public Optional<User> getById(String id) {
        try {
            String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
            List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
            
            if (users.isEmpty()) {
                logger.debug("User not found: {}", id);
                return Optional.empty();
            }
            
            logger.debug("User retrieved successfully: {}", id);
            return Optional.of(users.get(0));
        } catch (Exception e) {
            logger.error("Error retrieving user from RDBMS", e);
            throw new RuntimeException("Failed to get user", e);
        }
    }
    
    /**
     * Retrieve all users from the database
     * 
     * Users are ordered by creation time in descending order (newest first).
     * 
     * @return List of all User objects in the database
     * @throws RuntimeException if database operation fails
     */
    @Override
    public List<User> getAllUsers() {
        try {
            String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
            List<User> users = jdbcTemplate.query(sql, userRowMapper);
            
            logger.debug("Retrieved {} users from database", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error retrieving all users from RDBMS", e);
            throw new RuntimeException("Failed to get all users", e);
        }
    }
    
    /**
     * Update an existing user's information
     * 
     * This method:
     * 1. Verifies the user exists
     * 2. Updates all mutable fields (name, age, city, company, interests)
     * 3. Updates the modified timestamp
     * 4. Leaves ID and creation timestamp unchanged
     * 
     * @param id the unique identifier of the user to update
     * @param user the User object containing updated information
     * @return the updated User object
     * @throws RuntimeException if user not found or database operation fails
     */
    @Override
    public User update(String id, User user) {
        try {
            if (!exists(id)) {
                throw new RuntimeException("User not found with ID: " + id);
            }
            
            user.setId(id);
            user.setUpdatedAt(System.currentTimeMillis());
            
            String interestsJson = convertInterestsToJson(user.getInterests());
            
            String sql = "UPDATE " + TABLE_NAME + 
                         " SET name = ?, age = ?, city = ?, company = ?, interests = ?, updated_at = ? " +
                         "WHERE id = ?";
            
            jdbcTemplate.update(sql,
                user.getName(),
                user.getAge(),
                user.getCity(),
                user.getCompany(),
                interestsJson,
                user.getUpdatedAt(),
                id
            );
            
            logger.info("User updated successfully: {}", id);
            return user;
        } catch (Exception e) {
            logger.error("Error updating user in RDBMS", e);
            throw new RuntimeException("Failed to update user", e);
        }
    }
    
    /**
     * Delete a user record from the database
     * 
     * Deletes the user record and any associated child records
     * (e.g., audit log entries) due to foreign key constraints.
     * 
     * @param id the unique identifier of the user to delete
     * @return true if user was successfully deleted, false if user not found
     * @throws RuntimeException if database operation fails
     */
    @Override
    public boolean delete(String id) {
        try {
            if (!exists(id)) {
                logger.debug("User not found for deletion: {}", id);
                return false;
            }
            
            String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
            int rowsDeleted = jdbcTemplate.update(sql, id);
            
            logger.info("User deleted successfully: {}", id);
            return rowsDeleted > 0;
        } catch (Exception e) {
            logger.error("Error deleting user from RDBMS", e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }
    
    /**
     * Check if a user exists in the database
     * 
     * @param id the unique user identifier
     * @return true if user exists, false otherwise
     */
    @Override
    public boolean exists(String id) {
        try {
            String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking user existence in RDBMS", e);
            return false;
        }
    }
    
    /**
     * Get the total count of users in the database
     * 
     * @return the total number of user records
     */
    @Override
    public long count() {
        try {
            String sql = "SELECT COUNT(*) FROM " + TABLE_NAME;
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Error counting users in RDBMS", e);
            return 0;
        }
    }
    
    /**
     * Convert a list of interests to JSON string format
     * 
     * Transforms a List<String> into a JSON array representation.
     * Example: ["reading", "coding"] -> ["reading","coding"]
     * 
     * @param interests the list of interest strings (can be null)
     * @return JSON array string representation of interests
     */
    private String convertInterestsToJson(List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < interests.size(); i++) {
            json.append("\"").append(interests.get(i)).append("\"");
            if (i < interests.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }
    
    /**
     * Parse a JSON string into a list of interests
     * 
     * Extracts interest strings from a JSON array representation.
     * Example: ["reading","coding"] -> ["reading", "coding"]
     * 
     * @param json the JSON array string (can be null or empty)
     * @return List of interest strings (never null, empty list if input is empty)
     */
    private List<String> parseInterests(String json) {
        List<String> interests = new ArrayList<>();
        if (json == null || json.isEmpty() || "[]".equals(json)) {
            return interests;
        }
        
        // Simple JSON array parsing (can be enhanced with Jackson if needed)
        String content = json.substring(1, json.length() - 1); // Remove [ and ]
        if (!content.isEmpty()) {
            String[] items = content.split(",");
            for (String item : items) {
                interests.add(item.replaceAll("\"", "").trim());
            }
        }
        return interests;
    }
}
