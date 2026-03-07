package com.agentic.subscription.dao;

import com.agentic.subscription.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RdbmsUserDAO
 * Uses H2 in-memory database for testing
 */
@JdbcTest
@TestPropertySource(properties = {
    "spring.h2.console.enabled=true",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.initialization-mode=always"
})
public class RdbmsUserDAOTest {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private RdbmsUserDAO userDAO;
    
    @BeforeEach
    public void setUp() {
        // Create test users table
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "  id VARCHAR(36) PRIMARY KEY," +
            "  name VARCHAR(255) NOT NULL," +
            "  age INT," +
            "  city VARCHAR(255)," +
            "  company VARCHAR(255)," +
            "  interests JSON," +
            "  created_at BIGINT NOT NULL," +
            "  updated_at BIGINT NOT NULL" +
            ")"
        );
        
        // Clear any existing data
        jdbcTemplate.execute("TRUNCATE TABLE users");
        
        userDAO = new RdbmsUserDAO(jdbcTemplate);
    }
    
    private User createTestUser(String name) {
        User user = new User();
        user.setName(name);
        user.setAge(30);
        user.setCity("Test City");
        user.setCompany("Test Corp");
        user.setInterests(Arrays.asList("Java", "Testing"));
        return user;
    }
    
    @Test
    public void testCreate_Success() {
        // Given
        User user = createTestUser("John Doe");
        
        // When
        User created = userDAO.create(user);
        
        // Then
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("John Doe", created.getName());
        assertEquals(30, created.getAge());
        assertEquals("Test City", created.getCity());
        assertEquals("Test Corp", created.getCompany());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }
    
    @Test
    public void testGetById_Found() {
        // Given
        User user = createTestUser("John Doe");
        User created = userDAO.create(user);
        
        // When
        Optional<User> found = userDAO.getById(created.getId());
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals(30, found.get().getAge());
    }
    
    @Test
    public void testGetById_NotFound() {
        // When
        Optional<User> found = userDAO.getById("nonexistent-id");
        
        // Then
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testGetAllUsers() {
        // Given
        userDAO.create(createTestUser("John Doe"));
        userDAO.create(createTestUser("Jane Smith"));
        userDAO.create(createTestUser("Bob Johnson"));
        
        // When
        List<User> users = userDAO.getAllUsers();
        
        // Then
        assertEquals(3, users.size());
    }
    
    @Test
    public void testGetAllUsers_Empty() {
        // When
        List<User> users = userDAO.getAllUsers();
        
        // Then
        assertTrue(users.isEmpty());
    }
    
    @Test
    public void testUpdate_Success() {
        // Given
        User user = createTestUser("John Doe");
        User created = userDAO.create(user);
        String userId = created.getId();
        
        User updateData = createTestUser("Updated Name");
        updateData.setAge(35);
        updateData.setCity("New York");
        
        // When
        User updated = userDAO.update(userId, updateData);
        
        // Then
        assertNotNull(updated);
        assertEquals(userId, updated.getId());
        assertEquals("Updated Name", updated.getName());
        assertEquals(35, updated.getAge());
        assertEquals("New York", updated.getCity());
        assertEquals("Test Corp", updated.getCompany());
        assertNotNull(updated.getUpdatedAt());
        
        // Verify by retrieving from database
        Optional<User> retrieved = userDAO.getById(userId);
        assertTrue(retrieved.isPresent());
        assertEquals("Updated Name", retrieved.get().getName());
    }
    
    @Test
    public void testUpdate_NotFound() {
        // Given
        User updateData = createTestUser("Update");
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userDAO.update("nonexistent-id", updateData);
        });
    }
    
    @Test
    public void testDelete_Success() {
        // Given
        User user = createTestUser("John Doe");
        User created = userDAO.create(user);
        String userId = created.getId();
        
        // When
        boolean deleted = userDAO.delete(userId);
        
        // Then
        assertTrue(deleted);
        assertFalse(userDAO.getById(userId).isPresent());
    }
    
    @Test
    public void testDelete_NotFound() {
        // When
        boolean deleted = userDAO.delete("nonexistent-id");
        
        // Then
        assertFalse(deleted);
    }
    
    @Test
    public void testExists_True() {
        // Given
        User user = createTestUser("John Doe");
        User created = userDAO.create(user);
        
        // When
        boolean exists = userDAO.exists(created.getId());
        
        // Then
        assertTrue(exists);
    }
    
    @Test
    public void testExists_False() {
        // When
        boolean exists = userDAO.exists("nonexistent-id");
        
        // Then
        assertFalse(exists);
    }
    
    @Test
    public void testCount_Multiple() {
        // Given
        userDAO.create(createTestUser("John Doe"));
        userDAO.create(createTestUser("Jane Smith"));
        userDAO.create(createTestUser("Bob Johnson"));
        
        // When
        long count = userDAO.count();
        
        // Then
        assertEquals(3, count);
    }
    
    @Test
    public void testCount_Empty() {
        // When
        long count = userDAO.count();
        
        // Then
        assertEquals(0, count);
    }
    
    @Test
    public void testInterestsPersistence() {
        // Given
        User user = createTestUser("John Doe");
        List<String> interests = Arrays.asList("Java", "Spring", "Docker", "Kubernetes");
        user.setInterests(interests);
        
        // When
        User created = userDAO.create(user);
        Optional<User> retrieved = userDAO.getById(created.getId());
        
        // Then
        assertTrue(retrieved.isPresent());
        assertNotNull(retrieved.get().getInterests());
        assertEquals(interests.size(), retrieved.get().getInterests().size());
        assertTrue(retrieved.get().getInterests().contains("Java"));
        assertTrue(retrieved.get().getInterests().contains("Spring"));
    }
}
