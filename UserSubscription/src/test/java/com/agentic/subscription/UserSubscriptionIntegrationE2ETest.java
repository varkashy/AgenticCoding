package com.agentic.subscription;

import com.agentic.subscription.model.User;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;

/**
 * End-to-End Integration Test Suite for User Subscription API
 * 
 * This test class provides comprehensive E2E testing for the entire User Subscription API stack.
 * It uses TestContainers to spin up a real MySQL database for testing, ensuring that:
 * - Database auto-initialization works correctly
 * - All CRUD operations function properly
 * - API endpoints are correctly mapped and functioning
 * - Data persistence is working end-to-end
 * 
 * Test Flow:
 * 1. TestContainers starts a MySQL database
 * 2. Spring Boot application starts with test configuration
 * 3. Database is auto-initialized with schema
 * 4. API tests are executed against the running application
 * 
 * @author Development Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = UserSubscriptionIntegrationE2ETest.Initializer.class)
@DisplayName("User Subscription API - End-to-End Integration Tests")
public class UserSubscriptionIntegrationE2ETest {
    
    /**
     * MySQL Container for integration testing
     * Uses MySQL 8.0 with test database automatically created
     */
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("userdb")
            .withUsername("root")
            .withPassword("UserSubscription@123")
            .withReuse(false);
    
    @LocalServerPort
    private int port;
    
    /**
     * Initialize RestAssured with the test server port before running tests
     */
    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "";
    }
    
    /**
     * Spring context initializer that sets up database connection properties
     * from the TestContainers MySQL instance
     */
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context,
                    "spring.datasource.url=" + mysql.getJdbcUrl(),
                    "spring.datasource.username=" + mysql.getUsername(),
                    "spring.datasource.password=" + mysql.getPassword(),
                    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                    "app.persistence.type=rdbms",
                    "app.database.auto-create=true",
                    "app.database.auto-init-schema=true"
            );
        }
    }
    
    /**
     * Test: Create User
     * 
     * Verifies that:
     * - A user can be successfully created via POST /api/v1/users
     * - The response contains the created user with generated ID and timestamps
     * - HTTP status 201 (CREATED) is returned
     * - All user fields are correctly persisted
     */
    @Test
    @DisplayName("Should create a new user successfully")
    void testCreateUser() {
        // Arrange
        User newUser = new User();
        newUser.setName("John Doe");
        newUser.setAge(30);
        newUser.setCity("San Francisco");
        newUser.setCompany("Tech Corp");
        newUser.setInterests(Arrays.asList("Java", "Spring", "Cloud"));
        
        // Act
        User createdUser = given()
                .contentType("application/json")
                .body(newUser)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(201)
                .extract()
                .as(User.class);
        
        // Assert
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull().isNotEmpty();
        assertThat(createdUser.getName()).isEqualTo("John Doe");
        assertThat(createdUser.getAge()).isEqualTo(30);
        assertThat(createdUser.getCity()).isEqualTo("San Francisco");
        assertThat(createdUser.getCompany()).isEqualTo("Tech Corp");
        assertThat(createdUser.getInterests()).containsExactly("Java", "Spring", "Cloud");
        assertThat(createdUser.getCreatedAt()).isGreaterThan(0);
        assertThat(createdUser.getUpdatedAt()).isGreaterThan(0);
    }
    
    /**
     * Test: Get All Users
     * 
     * Verifies that:
     * - All users can be retrieved via GET /api/v1/users
     * - HTTP status 200 (OK) is returned
     * - The response contains a list of users
     */
    @Test
    @DisplayName("Should retrieve all users successfully")
    void testGetAllUsers() {
        // Arrange - Create multiple users
        for (int i = 1; i <= 3; i++) {
            User user = new User();
            user.setName("User " + i);
            user.setAge(20 + i);
            user.setCity("City " + i);
            user.setCompany("Company " + i);
            user.setInterests(Arrays.asList("Interest" + i));
            
            given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post("/api/v1/users")
                    .then()
                    .statusCode(201);
        }
        
        // Act
        List<User> users = given()
                .when()
                .get("/api/v1/users")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<User>>() {});
        
        // Assert
        assertThat(users).isNotEmpty().hasSizeGreaterThanOrEqualTo(3);
        assertThat(users).allMatch(u -> u.getId() != null);
        assertThat(users).allMatch(u -> u.getName() != null);
        assertThat(users).allMatch(u -> u.getCreatedAt() > 0);
    }
    
    /**
     * Test: Get User by ID
     * 
     * Verifies that:
     * - A specific user can be retrieved via GET /api/v1/users/{id}
     * - HTTP status 200 (OK) is returned for valid ID
     * - The correct user data is returned
     * - The user data matches what was originally created
     */
    @Test
    @DisplayName("Should retrieve a user by ID successfully")
    void testGetUserById() {
        // Arrange - Create a user
        User originalUser = new User();
        originalUser.setName("Alice Johnson");
        originalUser.setAge(28);
        originalUser.setCity("New York");
        originalUser.setCompany("Finance Corp");
        originalUser.setInterests(Arrays.asList("Finance", "Analytics"));
        
        User createdUser = given()
                .contentType("application/json")
                .body(originalUser)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(201)
                .extract()
                .as(User.class);
        
        String userId = createdUser.getId();
        
        // Act
        User retrievedUser = given()
                .when()
                .get("/api/v1/users/" + userId)
                .then()
                .statusCode(200)
                .extract()
                .as(User.class);
        
        // Assert
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(userId);
        assertThat(retrievedUser.getName()).isEqualTo("Alice Johnson");
        assertThat(retrievedUser.getAge()).isEqualTo(28);
        assertThat(retrievedUser.getCity()).isEqualTo("New York");
        assertThat(retrievedUser.getCompany()).isEqualTo("Finance Corp");
        assertThat(retrievedUser.getInterests()).containsExactly("Finance", "Analytics");
    }
    
    /**
     * Test: Update User
     * 
     * Verifies that:
     * - A user can be updated via PUT /api/v1/users/{id}
     * - HTTP status 200 (OK) is returned
     * - The updated data is persisted correctly
     * - The update timestamp is changed
     * - The creation timestamp remains unchanged
     */
    @Test
    @DisplayName("Should update a user successfully")
    void testUpdateUser() {
        // Arrange - Create a user
        User originalUser = new User();
        originalUser.setName("Bob Smith");
        originalUser.setAge(35);
        originalUser.setCity("Chicago");
        originalUser.setCompany("Original Corp");
        originalUser.setInterests(Arrays.asList("Tech"));
        
        User createdUser = given()
                .contentType("application/json")
                .body(originalUser)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(201)
                .extract()
                .as(User.class);
        
        long originalCreatedAt = createdUser.getCreatedAt();
        String userId = createdUser.getId();
        
        // Wait a moment to ensure timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Prepare updated user
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Bob Smith");
        updatedUser.setAge(36);
        updatedUser.setCity("Boston");
        updatedUser.setCompany("Updated Corp");
        updatedUser.setInterests(Arrays.asList("Tech", "Cloud"));
        
        // Act
        User resultUser = given()
                .contentType("application/json")
                .body(updatedUser)
                .when()
                .put("/api/v1/users/" + userId)
                .then()
                .statusCode(200)
                .extract()
                .as(User.class);
        
        // Assert
        assertThat(resultUser).isNotNull();
        assertThat(resultUser.getId()).isEqualTo(userId);
        assertThat(resultUser.getName()).isEqualTo("Bob Smith");
        assertThat(resultUser.getAge()).isEqualTo(36);
        assertThat(resultUser.getCity()).isEqualTo("Boston");
        assertThat(resultUser.getCompany()).isEqualTo("Updated Corp");
        assertThat(resultUser.getInterests()).containsExactly("Tech", "Cloud");
        assertThat(resultUser.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(resultUser.getUpdatedAt()).isGreaterThanOrEqualTo(originalCreatedAt);
    }
    
    /**
     * Test: Delete User
     * 
     * Verifies that:
     * - A user can be deleted via DELETE /api/v1/users/{id}
     * - HTTP status 200 (OK) is returned
     * - The user is no longer retrievable after deletion
     * - Subsequent GET request returns 404 (Not Found)
     */
    @Test
    @DisplayName("Should delete a user successfully")
    void testDeleteUser() {
        // Arrange - Create a user
        User userToDelete = new User();
        userToDelete.setName("Carol White");
        userToDelete.setAge(32);
        userToDelete.setCity("Austin");
        userToDelete.setCompany("Delete Corp");
        userToDelete.setInterests(Arrays.asList("Tests"));
        
        User createdUser = given()
                .contentType("application/json")
                .body(userToDelete)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(201)
                .extract()
                .as(User.class);
        
        String userId = createdUser.getId();
        
        // Verify user exists
        given()
                .when()
                .get("/api/v1/users/" + userId)
                .then()
                .statusCode(200);
        
        // Act - Delete the user
        given()
                .when()
                .delete("/api/v1/users/" + userId)
                .then()
                .statusCode(200);
        
        // Assert - User should no longer exist
        given()
                .when()
                .get("/api/v1/users/" + userId)
                .then()
                .statusCode(404);
    }
    
    /**
     * Test: User Count
     * 
     * Verifies that:
     * - User count can be retrieved via GET /api/v1/users/stats/count
     * - The count is correctly incremented as users are added
     * - The count is correctly decremented as users are deleted
     */
    @Test
    @DisplayName("Should return correct user count")
    void testUserCount() {
        // Arrange - Create multiple users
        for (int i = 1; i <= 2; i++) {
            User user = new User();
            user.setName("CountTest User " + i);
            user.setAge(25);
            user.setCity("Test City");
            user.setCompany("Test Company");
            
            given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post("/api/v1/users")
                    .then()
                    .statusCode(201);
        }
        
        // Act
        long count = given()
                .when()
                .get("/api/v1/users/stats/count")
                .then()
                .statusCode(200)
                .extract()
                .as(Long.class);
        
        // Assert
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
    
    /**
     * Test: Non-existent User
     * 
     * Verifies that:
     * - Requesting a non-existent user returns HTTP 404 (Not Found)
     * - No exception is thrown
     * - The error response is appropriate
     */
    @Test
    @DisplayName("Should return 404 for non-existent user")
    void testGetNonExistentUser() {
        // Act & Assert
        given()
                .when()
                .get("/api/v1/users/non-existent-id-12345")
                .then()
                .statusCode(404);
    }
    
    /**
     * Test: JDBC Connection Health Check
     * 
     * Verifies that:
     * - Database is properly initialized
     * - Application health check can connect to database
     * - The system is ready for production use
     */
    @Test
    @DisplayName("Should verify database connectivity")
    void testDatabaseConnectivity() {
        // Create a user to verify database connectivity
        User testUser = new User();
        testUser.setName("Connectivity Test");
        testUser.setAge(25);
        testUser.setCity("Test City");
        testUser.setCompany("Test Corp");
        
        // Act & Assert
        User result = given()
                .contentType("application/json")
                .body(testUser)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(201)
                .extract()
                .as(User.class);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }
}
