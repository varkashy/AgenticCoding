package com.agentic.subscription.service;

import com.agentic.subscription.dao.UserDAO;
import com.agentic.subscription.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserDAO userDAO;
    
    private UserService userService;
    
    @BeforeEach
    public void setUp() {
        userService = new UserService(userDAO);
    }
    
    @Test
    public void testCreateUser_Success() {
        // Given
        User user = new User();
        user.setName("John Doe");
        user.setAge(30);
        user.setCity("Austin");
        user.setCompany("Tech Corp");
        user.setInterests(Arrays.asList("Java", "Cloud"));
        
        User savedUser = new User();
        savedUser.setId("user-1");
        savedUser.setName("John Doe");
        savedUser.setAge(30);
        savedUser.setCity("Austin");
        savedUser.setCompany("Tech Corp");
        savedUser.setInterests(Arrays.asList("Java", "Cloud"));
        
        when(userDAO.create(any(User.class))).thenReturn(savedUser);
        
        // When
        User result = userService.createUser(user);
        
        // Then
        assertNotNull(result);
        assertEquals("user-1", result.getId());
        assertEquals("John Doe", result.getName());
        verify(userDAO, times(1)).create(any(User.class));
    }
    
    @Test
    public void testCreateUser_InvalidName() {
        // Given
        User user = new User();
        user.setName("");
        user.setAge(30);
        user.setInterests(Arrays.asList("Java"));
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        verify(userDAO, never()).create(any(User.class));
    }
    
    @Test
    public void testCreateUser_InvalidAge() {
        // Given
        User user = new User();
        user.setName("John Doe");
        user.setAge(200);
        user.setInterests(Arrays.asList("Java"));
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        verify(userDAO, never()).create(any(User.class));
    }
    
    @Test
    public void testGetUserById_Found() {
        // Given
        User user = new User();
        user.setId("user-1");
        user.setName("John Doe");
        
        when(userDAO.getById("user-1")).thenReturn(Optional.of(user));
        
        // When
        Optional<User> result = userService.getUserById("user-1");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(userDAO, times(1)).getById("user-1");
    }
    
    @Test
    public void testGetUserById_NotFound() {
        // Given
        when(userDAO.getById("nonexistent")).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userService.getUserById("nonexistent");
        
        // Then
        assertFalse(result.isPresent());
        verify(userDAO, times(1)).getById("nonexistent");
    }
    
    @Test
    public void testGetAllUsers() {
        // Given
        User user1 = new User();
        user1.setId("user-1");
        user1.setName("John Doe");
        
        User user2 = new User();
        user2.setId("user-2");
        user2.setName("Jane Smith");
        
        List<User> users = Arrays.asList(user1, user2);
        when(userDAO.getAllUsers()).thenReturn(users);
        
        // When
        List<User> result = userService.getAllUsers();
        
        // Then
        assertEquals(2, result.size());
        verify(userDAO, times(1)).getAllUsers();
    }
    
    @Test
    public void testUpdateUser_Success() {
        // Given
        String userId = "user-1";
        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setAge(35);
        updateData.setCity("New York");
        updateData.setCompany("New Corp");
        updateData.setInterests(Arrays.asList("Python"));
        
        when(userDAO.exists(userId)).thenReturn(true);
        when(userDAO.update(userId, updateData)).thenReturn(updateData);
        
        // When
        User result = userService.updateUser(userId, updateData);
        
        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(userDAO, times(1)).exists(userId);
        verify(userDAO, times(1)).update(userId, updateData);
    }
    
    @Test
    public void testUpdateUser_NotFound() {
        // Given
        String userId = "nonexistent";
        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setInterests(Arrays.asList("Java"));
        
        when(userDAO.exists(userId)).thenReturn(false);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userId, updateData));
        verify(userDAO, times(1)).exists(userId);
        verify(userDAO, never()).update(anyString(), any(User.class));
    }
    
    @Test
    public void testDeleteUser_Success() {
        // Given
        String userId = "user-1";
        when(userDAO.exists(userId)).thenReturn(true);
        when(userDAO.delete(userId)).thenReturn(true);
        
        // When
        boolean result = userService.deleteUser(userId);
        
        // Then
        assertTrue(result);
        verify(userDAO, times(1)).exists(userId);
        verify(userDAO, times(1)).delete(userId);
    }
    
    @Test
    public void testDeleteUser_NotFound() {
        // Given
        String userId = "nonexistent";
        when(userDAO.exists(userId)).thenReturn(false);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(userId));
        verify(userDAO, times(1)).exists(userId);
        verify(userDAO, never()).delete(anyString());
    }
    
    @Test
    public void testUserExists() {
        // Given
        when(userDAO.exists("user-1")).thenReturn(true);
        when(userDAO.exists("nonexistent")).thenReturn(false);
        
        // When & Then
        assertTrue(userService.userExists("user-1"));
        assertFalse(userService.userExists("nonexistent"));
    }
    
    @Test
    public void testGetUserCount() {
        // Given
        when(userDAO.count()).thenReturn(5L);
        
        // When
        long count = userService.getUserCount();
        
        // Then
        assertEquals(5L, count);
        verify(userDAO, times(1)).count();
    }
}
