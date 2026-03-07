package com.agentic.subscription.controller;

import com.agentic.subscription.model.User;
import com.agentic.subscription.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    @Test
    public void testCreateUser_Success() throws Exception {
        // Given
        User requestUser = new User();
        requestUser.setName("John Doe");
        requestUser.setAge(30);
        requestUser.setCity("Austin");
        requestUser.setCompany("Tech Corp");
        requestUser.setInterests(Arrays.asList("Java", "Cloud"));
        
        User savedUser = new User();
        savedUser.setId("user-1");
        savedUser.setName("John Doe");
        savedUser.setAge(30);
        savedUser.setCity("Austin");
        savedUser.setCompany("Tech Corp");
        savedUser.setInterests(Arrays.asList("Java", "Cloud"));
        savedUser.setCreatedAt(System.currentTimeMillis());
        savedUser.setUpdatedAt(System.currentTimeMillis());
        
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("user-1")))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.age", is(30)))
                .andExpect(jsonPath("$.city", is("Austin")))
                .andExpect(jsonPath("$.company", is("Tech Corp")));
        
        verify(userService, times(1)).createUser(any(User.class));
    }
    
    @Test
    public void testListAllUsers_Success() throws Exception {
        // Given
        User user1 = new User();
        user1.setId("user-1");
        user1.setName("John Doe");
        user1.setAge(30);
        user1.setCity("Austin");
        user1.setCompany("Tech Corp");
        
        User user2 = new User();
        user2.setId("user-2");
        user2.setName("Jane Smith");
        user2.setAge(28);
        user2.setCity("New York");
        user2.setCompany("Finance Corp");
        
        List<User> users = Arrays.asList(user1, user2);
        when(userService.getAllUsers()).thenReturn(users);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("user-1")))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].id", is("user-2")))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")));
        
        verify(userService, times(1)).getAllUsers();
    }
    
    @Test
    public void testGetUserById_Found() throws Exception {
        // Given
        User user = new User();
        user.setId("user-1");
        user.setName("John Doe");
        user.setAge(30);
        
        when(userService.getUserById("user-1")).thenReturn(Optional.of(user));
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/user-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("user-1")))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.age", is(30)));
        
        verify(userService, times(1)).getUserById("user-1");
    }
    
    @Test
    public void testGetUserById_NotFound() throws Exception {
        // Given
        when(userService.getUserById("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        
        verify(userService, times(1)).getUserById("nonexistent");
    }
    
    @Test
    public void testUpdateUser_Success() throws Exception {
        // Given
        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setAge(35);
        updateData.setCity("New York");
        updateData.setCompany("New Corp");
        updateData.setInterests(Arrays.asList("Python"));
        
        User updatedUser = new User();
        updatedUser.setId("user-1");
        updatedUser.setName("Updated Name");
        updatedUser.setAge(35);
        updatedUser.setCity("New York");
        updatedUser.setCompany("New Corp");
        updatedUser.setInterests(Arrays.asList("Python"));
        
        when(userService.updateUser(eq("user-1"), any(User.class))).thenReturn(updatedUser);
        
        // When & Then
        mockMvc.perform(put("/api/v1/users/user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("user-1")))
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.age", is(35)));
        
        verify(userService, times(1)).updateUser(eq("user-1"), any(User.class));
    }
    
    @Test
    public void testDeleteUser_Success() throws Exception {
        // Given
        when(userService.deleteUser("user-1")).thenReturn(true);
        
        // When & Then
        mockMvc.perform(delete("/api/v1/users/user-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(userService, times(1)).deleteUser("user-1");
    }
    
    @Test
    public void testDeleteUser_NotFound() throws Exception {
        // Given
        when(userService.deleteUser("nonexistent")).thenThrow(new IllegalArgumentException("User not found"));
        
        // When & Then
        mockMvc.perform(delete("/api/v1/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        
        verify(userService, times(1)).deleteUser("nonexistent");
    }
    
    @Test
    public void testGetUserCount() throws Exception {
        // Given
        when(userService.getUserCount()).thenReturn(5L);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/stats/count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(5)));
        
        verify(userService, times(1)).getUserCount();
    }
    
    @Test
    public void testHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }
}
