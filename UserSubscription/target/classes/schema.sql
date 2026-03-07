-- User Subscription Database DDL
-- This script creates the necessary tables for the user subscription application

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY COMMENT 'Unique user identifier (UUID)',
    name VARCHAR(255) NOT NULL COMMENT 'User full name',
    age INT COMMENT 'User age',
    city VARCHAR(255) COMMENT 'User city',
    company VARCHAR(255) COMMENT 'User company',
    interests JSON COMMENT 'User interests as JSON array',
    created_at BIGINT NOT NULL COMMENT 'Creation timestamp in milliseconds',
    updated_at BIGINT NOT NULL COMMENT 'Last update timestamp in milliseconds',
    
    -- Indexes for better query performance
    INDEX idx_created_at (created_at) COMMENT 'Index on creation timestamp',
    INDEX idx_name (name) COMMENT 'Index on user name',
    INDEX idx_company (company) COMMENT 'Index on company'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User subscription records';

-- Create audit_log table (optional, for tracking changes)
CREATE TABLE IF NOT EXISTS audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    action VARCHAR(50) NOT NULL COMMENT 'CREATE, UPDATE, DELETE',
    changed_fields JSON COMMENT 'Fields that were changed',
    changed_at BIGINT NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit log for user operations';
