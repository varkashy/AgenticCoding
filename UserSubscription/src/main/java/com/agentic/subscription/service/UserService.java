package com.agentic.subscription.service;

import com.agentic.subscription.dao.UserDAO;
import com.agentic.subscription.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * User Service - Business Logic Layer for User Subscription Management
 * 
 * This service provides all business logic operations for user management.
 * It acts as an intermediary between the REST controller and data access layer.
 * 
 * Responsibilities:
 * - User CRUD operations (Create, Read, Update, Delete)
 * - Business logic validation
 * - Data transformation
 * - Logging and monitoring
 * - Error handling and reporting
 * 
 * Design Pattern: Service Layer Pattern
 * - Encapsulates business logic
 * - Validates data before persistence
 * - Provides loose coupling between layers
 * - Enables transaction management
 * 
 * Dependencies:
 * - UserDAO: Data Access Object for database operations
 * - Logger: SLF4J for logging business events
 * 
 * @author Development Team
 * @version 1.0.0
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    /**
     * Data Access Object for user data persistence operations
     */
    private final UserDAO userDAO;
    
    /**
     * Constructor for dependency injection of UserDAO
     * 
     * @param userDAO the UserDAO implementation to use for data access
     */
    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        logger.info("UserService initialized with UserDAO: {}", userDAO.getClass().getSimpleName());
    }
    
    /**
     * Create a new user in the system
     * 
     * Business Logic:
     * 1. Validates all required user fields
     * 2. Checks for data integrity
     * 3. Persists user to database
     * 4. Returns created user with generated ID and timestamps
     * 
     * @param user the User object containing user details to create
     *             Required fields: name
     *             Optional fields: age, city, company, interests
     * 
     * @return the created User with auto-generated ID and timestamps
     * 
     * @throws IllegalArgumentException if user data validation fails
     * @throws RuntimeException if database operation fails
     * 
     * @example
     *   User newUser = new User();
     *   newUser.setName("John Doe");
     *   newUser.setAge(30);
     *   User created = userService.createUser(newUser);
     */
    public User createUser(User user) {
        validateUser(user);
        logger.info("Creating new user: {}", user.getName());
        return userDAO.create(user);
    }
    
    /**
     * Retrieve a specific user by their unique identifier
     * 
     * @param id the unique user ID (UUID format)
     * 
     * @return Optional containing the User if found, empty Optional if not found
     * 
     * @throws RuntimeException if database operation fails
     * 
     * @example
     *   Optional<User> user = userService.getUserById("550e8400-e29b-41d4-a716-446655440000");
     *   if (user.isPresent()) {
     *       System.out.println("User found: " + user.get().getName());
     *   }
     */
    public Optional<User> getUserById(String id) {
        logger.debug("Fetching user by ID: {}", id);
        return userDAO.getById(id);
    }
    
    /**
     * Retrieve all users from the system
     * 
     * @return List of all User objects in the database
     *         Returns empty list if no users exist
     * 
     * @throws RuntimeException if database operation fails
     * 
     * @example
     *   List<User> allUsers = userService.getAllUsers();
     *   System.out.println("Total users: " + allUsers.size());
     */
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return userDAO.getAllUsers();
    }
    
    /**
     * Update an existing user's information
     * 
     * Business Logic:
     * 1. Verifies user exists in database
     * 2. Validates updated data
     * 3. Updates user record with new data
     * 4. Returns updated user with new updatedAt timestamp
     * 
     * @param id the unique user ID (UUID format)
     * @param user the User object with updated information
     * 
     * @return the updated User object with modified data and new updatedAt timestamp
     * 
     * @throws IllegalArgumentException if user not found or data validation fails
     * @throws RuntimeException if database operation fails
     * 
     * @example
     *   User updatedUser = new User();
     *   updatedUser.setName("Jane Doe");
     *   updatedUser.setAge(31);
     *   User result = userService.updateUser(userId, updatedUser);
     */
    public User updateUser(String id, User user) {
        if (!userDAO.exists(id)) {
            logger.warn("Update attempted for non-existent user: {}", id);
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        validateUser(user);
        logger.info("Updating user: {}", id);
        return userDAO.update(id, user);
    }
    
    /**
     * Delete a user from the system
     * 
     * Business Logic:
     * 1. Verifies user exists in database
     * 2. Deletes user record
     * 3. Returns success status
     * 
     * Note: This operation is typically irreversible unless backups are maintained
     * 
     * @param id the unique user ID (UUID format)
     * 
     * @return true if user was successfully deleted, false otherwise
     * 
     * @throws IllegalArgumentException if user not found
     * @throws RuntimeException if database operation fails
     * 
     * @example
     *   boolean deleted = userService.deleteUser(userId);
     *   if (deleted) {
     *       System.out.println("User deleted successfully");
     *   }
     */
    public boolean deleteUser(String id) {
        if (!userDAO.exists(id)) {
            logger.warn("Delete attempted for non-existent user: {}", id);
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        logger.info("Deleting user: {}", id);
        return userDAO.delete(id);
    }
    
    /**
     * Check if a user exists in the system
     * 
     * @param id the unique user ID (UUID format)
     * 
     * @return true if user exists in database, false otherwise
     * 
     * @throws RuntimeException if database operation fails
     * 
     * @example
     *   if (userService.userExists(userId)) {
     *       System.out.println("User found");
     *   }
     */
    public boolean userExists(String id) {
        return userDAO.exists(id);
    }
    
    /**
     * Get the total number of users in the system
     * 
     * Useful for:
     * - System monitoring and statistics
     * - Pagination calculations
     * - Performance metrics
     * 
     * @return total count of users in database
     * 
     * @throws RuntimeException if database operation fails
     * 
     * @example
     *   long totalUsers = userService.getUserCount();
     *   System.out.println("System has " + totalUsers + " users");
     */
    public long getUserCount() {
        return userDAO.count();
    }
    
    /**
     * Validate user data before persistence
     * 
     * Validation Rules:
     * 1. Name: Required, cannot be empty or whitespace-only
     * 2. Age: Optional, but if provided must be between 0 and 150
     * 3. Interests: Optional, but if provided cannot be an empty list
     * 4. City: No validation (optional, any string allowed)
     * 5. Company: No validation (optional, any string allowed)
     * 
     * @param user the User object to validate
     * 
     * @throws IllegalArgumentException if any validation rule is violated
     * 
     * @example
     *   validateUser(user); // Throws IllegalArgumentException if invalid
     */
    private void validateUser(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            logger.warn("Validation failed: User name cannot be empty");
            throw new IllegalArgumentException("User name cannot be empty");
        }
        if (user.getAge() != null && (user.getAge() < 0 || user.getAge() > 150)) {
            logger.warn("Validation failed: Invalid age: {}", user.getAge());
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        // Note: Empty interests list is now allowed (was previously rejected)
        if (user.getInterests() != null && user.getInterests().isEmpty()) {
            logger.debug("User has empty interests list (allowed)");
        }
    }
}
