package com.agentic.subscription.dao;

import com.agentic.subscription.model.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of UserDAO for local testing and development
 * Uses ConcurrentHashMap for thread-safe operations
 */
@Repository("inMemoryUserDAO")
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "in-memory")
public class InMemoryUserDAO implements UserDAO {
    
    private final Map<String, User> userStore = new ConcurrentHashMap<>();
    
    @Override
    public User create(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());
        userStore.put(user.getId(), user);
        return user;
    }
    
    @Override
    public Optional<User> getById(String id) {
        return Optional.ofNullable(userStore.get(id));
    }
    
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(userStore.values());
    }
    
    @Override
    public User update(String id, User user) {
        if (!userStore.containsKey(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        user.setId(id);
        user.setUpdatedAt(System.currentTimeMillis());
        userStore.put(id, user);
        return user;
    }
    
    @Override
    public boolean delete(String id) {
        return userStore.remove(id) != null;
    }
    
    @Override
    public boolean exists(String id) {
        return userStore.containsKey(id);
    }
    
    @Override
    public long count() {
        return userStore.size();
    }
}
