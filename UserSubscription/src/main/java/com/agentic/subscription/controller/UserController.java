package com.agentic.subscription.controller;

import com.agentic.subscription.model.User;
import com.agentic.subscription.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for User Subscription API
 * 
 * Provides comprehensive CRUD (Create, Read, Update, Delete) operations for user management.
 * All endpoints are documented with OpenAPI/Swagger annotations for automatic API documentation.
 * 
 * Supported Operations:
 * - Create new users
 * - Retrieve users by ID or list all users
 * - Update existing user information
 * - Delete users
 * - Get user statistics and health status
 * 
 * Base Path: /api/v1/users
 * All responses include appropriate HTTP status codes and error handling
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "APIs for managing user subscriptions")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    /**
     * Constructor for dependency injection
     * 
     * @param userService the UserService instance for business logic
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create a new user
     * 
     * Accepts a JSON payload containing user details and creates a new user record
     * in the database. A unique ID and timestamps are automatically generated.
     * 
     * HTTP Method: POST
     * Endpoint: /api/v1/users
     * Content-Type: application/json
     * 
     * @param user the user object containing name, age, city, company, and interests
     * @return ResponseEntity containing the created user with HTTP 201 (Created) status
     * 
     * @throws IllegalArgumentException if user data validation fails
     * @throws Exception if database operation fails
     */
    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user record with generated ID and timestamps")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> createUser(
            @RequestBody User user) {
        try {
            logger.info("POST request to create user: {}", user.getName());
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Retrieve all users
     * 
     * Fetches a list of all users from the database, ordered by creation time (newest first).
     * 
     * HTTP Method: GET
     * Endpoint: /api/v1/users
     * 
     * @return ResponseEntity containing a list of all users with HTTP 200 (OK) status
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users from the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<User>> listAllUsers() {
        try {
            logger.info("GET request to list all users");
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error listing users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Retrieve a specific user by ID
     * 
     * Fetches a single user record from the database using the provided user ID.
     * 
     * HTTP Method: GET
     * Endpoint: /api/v1/users/{id}
     * 
     * @param id the unique identifier of the user (UUID format)
     * @return ResponseEntity containing the user if found with HTTP 200 (OK) status,
     *         or HTTP 404 (Not Found) if user doesn't exist
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        try {
            logger.info("GET request to retrieve user: {}", id);
            Optional<User> user = userService.getUserById(id);
            return user.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            logger.error("Error retrieving user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update an existing user
     * 
     * Updates the information of an existing user identified by the provided ID.
     * Timestamps are automatically updated to reflect the modification time.
     * 
     * HTTP Method: PUT
     * Endpoint: /api/v1/users/{id}
     * Content-Type: application/json
     * 
     * @param id the unique identifier of the user to update
     * @param user the user object containing updated information
     * @return ResponseEntity containing the updated user with HTTP 200 (OK) status
     * 
     * @throws IllegalArgumentException if user ID is not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Updates an existing user's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> updateUser(
            @PathVariable String id,
            @RequestBody User user) {
        try {
            logger.info("PUT request to update user: {}", id);
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete a user
     * 
     * Removes a user record from the database permanently. 
     * Associated audit logs are also deleted due to foreign key constraints.
     * 
     * HTTP Method: DELETE
     * Endpoint: /api/v1/users/{id}
     * 
     * @param id the unique identifier of the user to delete
     * @return ResponseEntity with HTTP 204 (No Content) on success,
     *         or HTTP 404 (Not Found) if user doesn't exist
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user record from the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        try {
            logger.info("DELETE request for user: {}", id);
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (IllegalArgumentException e) {
            logger.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get user count statistics
     * 
     * Returns the total number of users currently stored in the database.
     * 
     * HTTP Method: GET
     * Endpoint: /api/v1/users/stats/count
     * 
     * @return ResponseEntity containing a CountResponse object with user count
     */
    @GetMapping("/stats/count")
    @Operation(summary = "Get user count", description = "Returns the total number of users in the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CountResponse> getUserCount() {
        try {
            logger.info("GET request for user count");
            long count = userService.getUserCount();
            return ResponseEntity.ok(new CountResponse(count));
        } catch (Exception e) {
            logger.error("Error getting user count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint
     * 
     * Simple health check to verify API is running and responsive.
     * Used for monitoring and load balancer health checks.
     * 
     * HTTP Method: GET
     * Endpoint: /api/v1/users/health
     * 
     * @return ResponseEntity containing HealthResponse with status "UP"
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks if the API service is running")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP"));
    }
    
    /**
     * Response DTO for user count endpoint
     * 
     * Simple data transfer object containing the total user count.
     */
    public static class CountResponse {
        /** Total number of users */
        public long count;
        
        /**
         * Constructor
         * 
         * @param count the total user count
         */
        public CountResponse(long count) {
            this.count = count;
        }
    }
    
    /**
     * Response DTO for health check endpoint
     * 
     * Simple data transfer object containing service health status.
     */
    public static class HealthResponse {
        /** Status of the service (e.g., "UP" or "DOWN") */
        public String status;
        
        /**
         * Constructor
         * 
         * @param status the service status
         */
        public HealthResponse(String status) {
            this.status = status;
        }
    }
}
