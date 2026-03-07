package com.agentic.subscription.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * User Domain Model for User Subscription Management
 * 
 * Represents a user record in the system with complete subscription management information.
 * This model is used throughout the application for data transfer between layers.
 * 
 * Persistence: Stored in RDBMS (MySQL, PostgreSQL, Oracle Autonomous Database)
 * 
 * Fields:
 * - id: Unique identifier (UUID v4 format)
 * - name: User's full name (required)
 * - age: User's age (optional)
 * - city: User's city (optional)
 * - company: User's company/organization (optional)
 * - interests: List of user interests (optional, stored as JSON in database)
 * - createdAt: Timestamp of record creation (milliseconds since epoch)
 * - updatedAt: Timestamp of last record update (milliseconds since epoch)
 * 
 * JSON Serialization: Uses Jackson annotations for automatic JSON conversion
 * 
 * @author Development Team
 * @version 1.0.0
 */
public class User {
    
    /**
     * Unique identifier for the user (UUID v4 format)
     * Example: "550e8400-e29b-41d4-a716-446655440000"
     */
    private String id;
    
    /**
     * User's full name
     * Required field - cannot be null
     */
    private String name;
    
    /**
     * User's age
     * Optional field
     */
    private Integer age;
    
    /**
     * User's city/location
     * Optional field
     */
    private String city;
    
    /**
     * User's company or organization
     * Optional field
     */
    private String company;
    
    /**
     * List of user's interests or areas of expertise
     * Stored as JSON in database for flexible storage
     * Example: ["Java", "Spring", "Cloud Computing"]
     * Optional field
     */
    private List<String> interests;
    
    /**
     * Timestamp when the user record was created
     * Stored as milliseconds since Unix epoch
     * Automatically set on creation
     */
    private Long createdAt;
    
    /**
     * Timestamp when the user record was last updated
     * Stored as milliseconds since Unix epoch
     * Updated on every modification
     */
    private Long updatedAt;
    
    /**
     * Default constructor for User
     * Instantiates an empty User object
     */
    public User() {
    }
    
    /**
     * Constructor for creating a User with basic information
     * 
     * @param name the user's full name
     * @param age the user's age
     * @param city the user's city
     * @param company the user's company
     * @param interests the user's list of interests
     */
    public User(String name, Integer age, String city, String company, List<String> interests) {
        this.name = name;
        this.age = age;
        this.city = city;
        this.company = company;
        this.interests = interests;
    }
    
    
    // ============ Getter and Setter Methods ============
    
    /**
     * Get the unique identifier for this user
     * 
     * @return the user's UUID, or null if not yet persisted
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    
    /**
     * Set the unique identifier for this user
     * 
     * @param id the user's UUID (typically set by system, not by clients)
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Get the user's full name
     * 
     * @return the user's name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    
    /**
     * Set the user's full name
     * 
     * @param name the user's name (required)
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the user's age
     * 
     * @return the user's age, or null if not provided
     */
    @JsonProperty("age")
    public Integer getAge() {
        return age;
    }
    
    /**
     * Set the user's age
     * 
     * @param age the user's age (optional)
     */
    @JsonProperty("age")
    public void setAge(Integer age) {
        this.age = age;
    }
    
    /**
     * Get the user's city/location
     * 
     * @return the user's city, or null if not provided
     */
    @JsonProperty("city")
    public String getCity() {
        return city;
    }
    
    /**
     * Set the user's city/location
     * 
     * @param city the user's city (optional)
     */
    @JsonProperty("city")
    public void setCity(String city) {
        this.city = city;
    }
    
    /**
     * Get the user's company/organization
     * 
     * @return the user's company, or null if not provided
     */
    @JsonProperty("company")
    public String getCompany() {
        return company;
    }
    
    /**
     * Set the user's company/organization
     * 
     * @param company the user's company (optional)
     */
    @JsonProperty("company")
    public void setCompany(String company) {
        this.company = company;
    }
    
    /**
     * Get the user's list of interests
     * 
     * @return list of interest strings, or null if not provided
     */
    @JsonProperty("interests")
    public List<String> getInterests() {
        return interests;
    }
    
    /**
     * Set the user's list of interests
     * 
     * @param interests list of user interests (optional, stored as JSON in database)
     */
    @JsonProperty("interests")
    public void setInterests(List<String> interests) {
        this.interests = interests;
    }
    
    /**
     * Get the timestamp when this record was created
     * 
     * @return creation timestamp in milliseconds since Unix epoch
     */
    @JsonProperty("createdAt")
    public Long getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set the timestamp when this record was created
     * 
     * @param createdAt creation timestamp in milliseconds since Unix epoch (system-set)
     */
    @JsonProperty("createdAt")
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get the timestamp when this record was last updated
     * 
     * @return last update timestamp in milliseconds since Unix epoch
     */
    @JsonProperty("updatedAt")
    public Long getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Set the timestamp when this record was last updated
     * 
     * @param updatedAt last update timestamp in milliseconds since Unix epoch (system-set)
     */
    @JsonProperty("updatedAt")
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // ============ Factory Methods ============
    
    /**
     * Factory method to create a new User with auto-generated ID and timestamps
     * 
     * This static method provides a convenient way to create a new User instance
     * with automatic ID generation (UUID v4) and timestamp initialization.
     * Used primarily for creating new users from API requests.
     * 
     * @param name the user's full name (required)
     * @param age the user's age (optional, can be null)
     * @param city the user's city/location (optional, can be null)
     * @param company the user's company/organization (optional, can be null)
     * @param interests list of user interests (optional, can be null)
     * 
     * @return a new User instance with generated UUID and current timestamps
     * 
     * @example
     *   User newUser = User.create("John Doe", 30, "San Francisco", 
     *                               "Tech Corp", Arrays.asList("Java", "Spring"));
     */
    public static User create(String name, Integer age, String city, String company, List<String> interests) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(name);
        user.setAge(age);
        user.setCity(city);
        user.setCompany(company);
        user.setInterests(interests);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());
        return user;
    }
    
    // ============ Object Methods ============
    
    /**
     * String representation of a User
     * 
     * @return a string containing all user properties in a readable format
     */
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", city='" + city + '\'' +
                ", company='" + company + '\'' +
                ", interests=" + interests +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

