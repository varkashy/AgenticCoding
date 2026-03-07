package com.agentic.subscription.dao;

import com.agentic.subscription.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryUserDAOTest {
    
    private InMemoryUserDAO userDAO;
    
    @BeforeEach
    public void setUp() {
        userDAO = new InMemoryUserDAO();
    }
    
    @Test
    public void testCreate_Success() {
        // Given
        User user = new User();
        user.setName("John Doe");
        user.setAge(30);
        user.setCity("Austin");
        user.setCompany("Tech Corp");
        user.setInterests(Arrays.asList("Java", "Cloud"));
        
        // When
        User created = userDAO.create(user);
        
        // Then
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("John Doe", created.getName());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }
    
    @Test
    public void testGetById_Found() {
        // Given
        User user = createAndSaveUser("John Doe");
        
        // When
        Optional<User> found = userDAO.getById(user.getId());
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
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
        createAndSaveUser("John Doe");
        createAndSaveUser("Jane Smith");
        createAndSaveUser("Bob Johnson");
        
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
        User user = createAndSaveUser("John Doe");
        String userId = user.getId();
        
        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setAge(35);
        updateData.setCity("New York");
        updateData.setCompany("New Corp");
        updateData.setInterests(Arrays.asList("Python", "AI"));
        
        // When
        User updated = userDAO.update(userId, updateData);
        
        // Then
        assertNotNull(updated);
        assertEquals(userId, updated.getId());
        assertEquals("Updated Name", updated.getName());
        assertEquals(35, updated.getAge());
        
        // Verify it's actually updated
        Optional<User> retrieved = userDAO.getById(userId);
        assertEquals("Updated Name", retrieved.get().getName());
    }
    
    @Test
    public void testUpdate_NotFound() {
        // Given
        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setInterests(Arrays.asList("Java"));
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userDAO.update("nonexistent", updateData));
    }
    
    @Test
    public void testDelete_Success() {
        // Given
        User user = createAndSaveUser("John Doe");
        String userId = user.getId();
        
        // When
        boolean deleted = userDAO.delete(userId);
        
        // Then
        assertTrue(deleted);
        assertFalse(userDAO.exists(userId));
    }
    
    @Test
    public void testDelete_NotFound() {
        // When
        boolean deleted = userDAO.delete("nonexistent");
        
        // Then
        assertFalse(deleted);
    }
    
    @Test
    public void testExists_True() {
        // Given
        User user = createAndSaveUser("John Doe");
        
        // When
        boolean exists = userDAO.exists(user.getId());
        
        // Then
        assertTrue(exists);
    }
    
    @Test
    public void testExists_False() {
        // When
        boolean exists = userDAO.exists("nonexistent");
        
        // Then
        assertFalse(exists);
    }
    
    @Test
    public void testCount() {
        // Given
        createAndSaveUser("John Doe");
        createAndSaveUser("Jane Smith");
        createAndSaveUser("Bob Johnson");
        
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
    
    /**
     * Helper method to create and save a user
     */
    private User createAndSaveUser(String name) {
        User user = new User();
        user.setName(name);
        user.setAge(30);
        user.setCity("Austin");
        user.setCompany("Tech Corp");
        user.setInterests(Arrays.asList("Java", "Cloud"));
        return userDAO.create(user);
    }
}
