package com.agentic.subscription.dao;

import com.agentic.subscription.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User operations
 * This interface abstracts the data persistence layer, allowing for different implementations
 * (OCI NoSQL, local storage, etc.) without changing the service layer
 */
public interface UserDAO {
    
    /**
     * Create a new user in the database
     * @param user the user to create
     * @return the created user with ID assigned
     */
    User create(User user);
    
    /**
     * Retrieve a user by ID
     * @param id the user ID
     * @return Optional containing the user if found
     */
    Optional<User> getById(String id);
    
    /**
     * Retrieve all users from the database
     * @return list of all users
     */
    List<User> getAllUsers();
    
    /**
     * Update an existing user
     * @param id the user ID
     * @param user the updated user data
     * @return the updated user
     */
    User update(String id, User user);
    
    /**
     * Delete a user by ID
     * @param id the user ID
     * @return true if user was deleted, false if not found
     */
    boolean delete(String id);
    
    /**
     * Check if a user exists
     * @param id the user ID
     * @return true if user exists
     */
    boolean exists(String id);
    
    /**
     * Get count of all users
     * @return total number of users
     */
    long count();
}
