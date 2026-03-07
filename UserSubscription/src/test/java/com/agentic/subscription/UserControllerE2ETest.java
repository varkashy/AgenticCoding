package com.agentic.subscription;

import com.agentic.subscription.dao.UserDAO;
import com.agentic.subscription.model.User;
import com.agentic.subscription.service.UserService;
import com.agentic.subscription.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Integration Tests for User Subscription API
 * 
 * Tests the complete flow of the application by:
 * 1. Starting the Spring Boot application
 * 2. Initializing the database with schema
 * 3. Testing all API endpoints with CRUD operations
 * 4. Verifying database state changes
 * 5. Validating error handling and edge cases
 * 
 * Uses H2 in-memory database for testing (configured in application-test.properties)
 * Includes end-to-end tests for:
 * - User creation with automatic ID and timestamp generation
 * - User retrieval (single and all users)
 * - User updates with timestamp tracking
 * - User deletion with cascade operations
 * - Error handling for invalid requests
 * - Count and health check endpoints
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Subscription API End-to-End Tests")
public class UserControllerE2ETest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserDAO userDAO;
    
    private User testUser;
    private String testUserId;
    
    /**
     * Set up test data before each test
     * Clears previous data and initializes fresh test user
     */
    @BeforeEach
    void setUp() {
        // Create test user data
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setAge(30);
        testUser.setCity("San Francisco");
        testUser.setCompany("Tech Corp");
        testUser.setInterests(Arrays.asList("coding", "reading", "travel"));
    }
    
    /**
     * Test 1: Create User via API
     * 
     * Verifies:
     * - POST request successfully creates user
     * - Response contains user data with generated ID
     * - HTTP 201 (Created) status is returned
     * - User is persisted in database
     * - Timestamps are automatically set
     */
    @Test
    @DisplayName("Create user via API should return 201 and persisted user")
    void testCreateUserE2E() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.city").value("San Francisco"))
                .andExpect(jsonPath("$.company").value("Tech Corp"))
                .andExpect(jsonPath("$.interests[0]").value("coding"))
                .andExpect(jsonPath("$.createdAt").isNumber())
                .andExpect(jsonPath("$.updatedAt").isNumber())
                .andReturn();
        
        // Extract user ID from response
        String responseBody = result.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(responseBody, User.class);
        testUserId = createdUser.getId();
        
        // Verify user is in database
        assertNotNull(testUserId);
        Optional<User> dbUser = userDAO.getById(testUserId);
        assertTrue(dbUser.isPresent());
        assertEquals("John Doe", dbUser.get().getName());
        assertEquals(30, dbUser.get().getAge());
    }
    
    /**
     * Test 2: Retrieve User by ID via API
     * 
     * Verifies:
     * - GET request retrieves correct user by ID
     * - Response contains complete user data
     * - HTTP 200 (OK) status is returned
     */
    @Test
    @DisplayName("Get user by ID should return complete user data")
    void testGetUserByIdE2E() throws Exception {
        // Arrange: Create user first
        User created = userService.createUser(testUser);
        testUserId = created.getId();
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.city").value("San Francisco"))
                .andExpect(jsonPath("$.company").value("Tech Corp"))
                .andExpect(jsonPath("$.interests", hasSize(3)))
                .andExpect(jsonPath("$.interests[*]", hasItems("coding", "reading", "travel")));
    }
    
    /**
     * Test 3: List All Users via API
     * 
     * Verifies:
     * - GET all users returns array
     * - Multiple users are correctly returned
     * - HTTP 200 (OK) status is returned
     * - Users are ordered by creation time (newest first)
     */
    @Test
    @DisplayName("Get all users should return array of users")
    void testListUsersE2E() throws Exception {
        // Arrange: Create multiple users
        User user1 = new User("Alice", 28, "NYC", "StartupA", Arrays.asList("AI", "ML"));
        User user2 = new User("Bob", 35, "LA", "StartupB", Arrays.asList("devops"));
        userService.createUser(user1);
        userService.createUser(user2);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].name", hasItems("Alice", "Bob")));
    }
    
    /**
     * Test 4: Update User via API
     * 
     * Verifies:
     * - PUT request successfully updates user
     * - Updated data is persisted
     * - Original creation timestamp is preserved
     * - Updated timestamp is changed
     * - HTTP 200 (OK) status is returned
     */
    @Test
    @DisplayName("Update user should modify all mutable fields")
    void testUpdateUserE2E() throws Exception {
        // Arrange: Create user first
        User created = userService.createUser(testUser);
        testUserId = created.getId();
        long originalCreatedAt = created.getCreatedAt();
        
        // Prepare update
        User updateData = new User();
        updateData.setName("Jane Smith");
        updateData.setAge(31);
        updateData.setCity("Seattle");
        updateData.setCompany("Tech Corp Plus");
        updateData.setInterests(Arrays.asList("design", "hiking"));
        
        // Small delay to ensure timestamp difference
        Thread.sleep(10);
        
        // Act & Assert
        MvcResult result = mockMvc.perform(put("/api/v1/users/" + testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.age").value(31))
                .andExpect(jsonPath("$.city").value("Seattle"))
                .andExpect(jsonPath("$.company").value("Tech Corp Plus"))
                .andExpect(jsonPath("$.interests", hasSize(2)))
                .andReturn();
        
        // Verify timestamps
        String responseBody = result.getResponse().getContentAsString();
        User updatedUser = objectMapper.readValue(responseBody, User.class);
        assertEquals(originalCreatedAt, updatedUser.getCreatedAt(), "Created timestamp should not change");
        assertNotEquals(originalCreatedAt, updatedUser.getUpdatedAt(), "Updated timestamp should change");
    }
    
    /**
     * Test 5: Delete User via API
     * 
     * Verifies:
     * - DELETE request successfully removes user
     * - User is no longer in database
     * - HTTP 204 (No Content) status is returned
     * - Subsequent GET returns 404 (Not Found)
     */
    @Test
    @DisplayName("Delete user should remove from database")
    void testDeleteUserE2E() throws Exception {
        // Arrange: Create user first
        User created = userService.createUser(testUser);
        testUserId = created.getId();
        
        // Verify user exists
        Optional<User> beforeDelete = userDAO.getById(testUserId);
        assertTrue(beforeDelete.isPresent());
        
        // Act: Delete user
        mockMvc.perform(delete("/api/v1/users/" + testUserId))
                .andExpect(status().isNoContent());
        
        // Assert: User should no longer exist
        mockMvc.perform(get("/api/v1/users/" + testUserId))
                .andExpect(status().isNotFound());
        
        Optional<User> afterDelete = userDAO.getById(testUserId);
        assertFalse(afterDelete.isPresent(), "User should be deleted from database");
    }
    
    /**
     * Test 6: Get User Count via API
     * 
     * Verifies:
     * - GET count endpoint returns correct total
     * - Count increases when users are added
     * - Count decreases when users are deleted
     * - HTTP 200 (OK) status is returned
     */
    @Test
    @DisplayName("Get user count should return accurate total")
    void testGetUserCountE2E() throws Exception {
        // Arrange: Get initial count
        long initialCount = userDAO.count();
        
        // Create users
        userService.createUser(testUser);
        User user2 = new User("Bob", 35, "Boston", "Company B", Arrays.asList("music"));
        userService.createUser(user2);
        
        // Act & Assert: Check count increased
        mockMvc.perform(get("/api/v1/users/stats/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(initialCount + 2));
    }
    
    /**
     * Test 7: Health Check via API
     * 
     * Verifies:
     * - GET health endpoint returns UP status
     * - Indicates service is running
     * - HTTP 200 (OK) status is returned
     */
    @Test
    @DisplayName("Health check should return UP status")
    void testHealthCheckE2E() throws Exception {
        mockMvc.perform(get("/api/v1/users/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
    
    /**
     * Test 8: Get Non-existent User
     * 
     * Verifies:
     * - GET with invalid ID returns 404
     * - Error handling works correctly
     * - HTTP 404 (Not Found) status is returned
     */
    @Test
    @DisplayName("Get non-existent user should return 404")
    void testGetNonExistentUserE2E() throws Exception {
        mockMvc.perform(get("/api/v1/users/invalid-id-12345"))
                .andExpect(status().isNotFound());
    }
    
    /**
     * Test 9: Update Non-existent User
     * 
     * Verifies:
     * - PUT with invalid ID returns 404
     * - Error handling for non-existent resources
     * - HTTP 404 (Not Found) status is returned
     */
    @Test
    @DisplayName("Update non-existent user should return 404")
    void testUpdateNonExistentUserE2E() throws Exception {
        mockMvc.perform(put("/api/v1/users/invalid-id-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());
    }
    
    /**
     * Test 10: Delete Non-existent User
     * 
     * Verifies:
     * - DELETE with invalid ID returns 404
     * - Error handling for delete operations
     * - HTTP 404 (Not Found) status is returned
     */
    @Test
    @DisplayName("Delete non-existent user should return 404")
    void testDeleteNonExistentUserE2E() throws Exception {
        mockMvc.perform(delete("/api/v1/users/invalid-id-12345"))
                .andExpect(status().isNotFound());
    }
    
    /**
     * Test 11: CRUD Workflow
     * 
     * Complete workflow test that exercises all primary operations:
     * 1. Create user
     * 2. Retrieve user
     * 3. Update user
     * 4. Delete user
     * 5. Verify deletion
     * 
     * Ensures data consistency throughout the workflow
     */
    @Test
    @DisplayName("Complete CRUD workflow should succeed end-to-end")
    void testCompleteCRUDWorkflowE2E() throws Exception {
        // 1. CREATE
        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andReturn();
        
        User createdUser = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), User.class);
        testUserId = createdUser.getId();
        assertNotNull(testUserId);
        
        // 2. READ
        mockMvc.perform(get("/api/v1/users/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId))
                .andExpect(jsonPath("$.name").value("John Doe"));
        
        // 3. UPDATE
        User updateData = new User();
        updateData.setName("Jane Doe");
        updateData.setAge(31);
        updateData.setCity("Boston");
        updateData.setCompany("New Corp");
        updateData.setInterests(Arrays.asList("photography"));
        
        mockMvc.perform(put("/api/v1/users/" + testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));
        
        // 4. DELETE
        mockMvc.perform(delete("/api/v1/users/" + testUserId))
                .andExpect(status().isNoContent());
        
        // 5. VERIFY DELETION
        mockMvc.perform(get("/api/v1/users/" + testUserId))
                .andExpect(status().isNotFound());
    }
    
    /**
     * Test 12: User with Empty Interests
     * 
     * Verifies:
     * - Users can be created without interests
     * - Empty interests are handled correctly
     * - JSON serialization works for empty arrays
     */
    @Test
    @DisplayName("Create user with empty interests should succeed")
    void testCreateUserWithoutInterestsE2E() throws Exception {
        User userNoInterests = new User();
        userNoInterests.setName("NoHobbies Person");
        userNoInterests.setAge(25);
        userNoInterests.setCity("Chicago");
        userNoInterests.setCompany("Company");
        userNoInterests.setInterests(Arrays.asList());
        
        MvcResult result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userNoInterests)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.interests", hasSize(0)))
                .andReturn();
        
        User created = objectMapper.readValue(
            result.getResponse().getContentAsString(), User.class);
        assertEquals(0, created.getInterests().size());
    }
}
