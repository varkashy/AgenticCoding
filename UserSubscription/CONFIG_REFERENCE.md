# Configuration Reference

## Property Overview

### Application Persistence Type

```yaml
app:
  persistence:
    type: rdbms  # Options: 'rdbms' or 'in-memory'
```

| Type | Repository | Database | Best For |
|------|-----------|----------|----------|
| `rdbms` | RdbmsUserDAO | MySQL, PostgreSQL, Oracle, etc. | Production, Cloud, Complex Queries |
| `in-memory` | InMemoryUserDAO | None (uses HashMap) | Testing, Development, Demo |

### Spring DataSource Configuration

#### JDBC Driver Configuration
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver  # MySQL
    # driver-class-name: org.postgresql.Driver    # PostgreSQL
    # driver-class-name: oracle.jdbc.driver.OracleDriver  # Oracle
```

#### Connection URL Examples

**MySQL Local:**
```
jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC
```

**MySQL OCI:**
```
jdbc:mysql://user-subscription-db.mysql.oraclecloud.com:3306/userdb?useSSL=true&serverTimezone=UTC
```

**PostgreSQL Local:**
```
jdbc:postgresql://localhost:5432/userdb
```

**PostgreSQL Cloud (Azure):**
```
jdbc:postgresql://servername.postgres.database.azure.com:5432/userdb?ssl=true
```

**Oracle:**
```
jdbc:oracle:thin:@localhost:1521:ORCL
```

#### Credentials
```yaml
spring:
  datasource:
    username: root
    password: ${DB_PASSWORD:UserSub@123}  # Can override via env variable
```

### Connection Pooling (HikariCP)

Default configuration in `application.yml`:
```yaml
spring:
  hikari:
    maximum-pool-size: 10        # Max concurrent connections
    minimum-idle: 5              # Idle connections to maintain
    connection-timeout: 30000    # Wait time for connection (ms)
    idle-timeout: 600000         # 10 minutes
    max-lifetime: 1800000        # 30 minutes
    auto-commit: true
```

**For OCI (high traffic):**
```yaml
spring:
  hikari:
    maximum-pool-size: 20
    minimum-idle: 5
    connection-timeout: 30000
    idle-timeout: 900000         # 15 minutes
    max-lifetime: 1800000
```

**For local development (low traffic):**
```yaml
spring:
  hikari:
    maximum-pool-size: 5
    minimum-idle: 1
    connection-timeout: 30000
    idle-timeout: 300000         # 5 minutes
    max-lifetime: 1800000
```

### Logging Configuration

```yaml
logging:
  level:
    root: INFO
    com.agentic.subscription: DEBUG        # Application logs
    org.springframework.jdbc: DEBUG         # JDBC logs
    org.springframework.boot: INFO          # Spring Boot logs
    
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

**Log Levels:**
- `DEBUG`: Detailed trace (development)
- `INFO`: General info (production)
- `WARN`: Warnings (always)
- `ERROR`: Errors (always)

### Server Configuration

```yaml
server:
  port: 8080                    # API port
  servlet:
    context-path: /            # API base path
    
  # Tomcat configuration
  tomcat:
    threads:
      max: 200
      min-spare: 10
    connection-timeout: 20000ms
```

### Actuator/Health Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics  # Exposed endpoints
        
  endpoint:
    health:
      show-details: always      # Always show detailed health
```

**Endpoints available:**
- `GET /actuator/health` - Application health
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Performance metrics

## Environment Variable Overrides

All Spring properties can be set via environment variables using the format:
```
SPRING_<PROPERTY_PATH>=value
```

Examples:
```bash
# Database configuration
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/userdb
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=MyPassword123

# Application settings
export APP_PERSISTENCE_TYPE=rdbms

# Logging
export LOGGING_LEVEL_COM_AGENTIC_SUBSCRIPTION=DEBUG

# Server
export SERVER_PORT=8080
```

## Spring Profiles

### Using Profiles

```bash
# Single profile
java -jar app.jar --spring.profiles.active=local

# Multiple profiles
java -jar app.jar --spring.profiles.active=oci,actuator

# Via environment variable
export SPRING_PROFILES_ACTIVE=local
java -jar app.jar
```

### Profile Configurations Included

**`local`** - Development on localhost
- File: `application-local.yml`
- Database: MySQL on localhost:3306
- Logging: DEBUG

**`oci`** - OCI Cloud deployment
- File: `application-oci.yml`
- Database: OCI MySQL Database System
- Logging: INFO
- Uses environment variables for secrets

## Database Schema Reference

### Users Table
```sql
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  age INT,
  city VARCHAR(255),
  company VARCHAR(255),
  interests JSON,                -- Format: ["interest1", "interest2"]
  created_at BIGINT NOT NULL,    -- Milliseconds since epoch
  updated_at BIGINT NOT NULL,
  
  INDEX idx_created_at (created_at),
  INDEX idx_name (name),
  INDEX idx_company (company)
);
```

### Queries

```sql
-- Get user by ID
SELECT * FROM users WHERE id = 'user-id-here';

-- Get all users sorted by creation date
SELECT * FROM users ORDER BY created_at DESC;

-- Search by name
SELECT * FROM users WHERE name LIKE '%John%';

-- Search by company
SELECT * FROM users WHERE company = 'Acme Corp';

-- Get statistics
SELECT COUNT(*) as total_users,
       MIN(created_at) as first_user,
       MAX(created_at) as recent_user
FROM users;

-- Find users with specific interest
SELECT * FROM users
WHERE JSON_CONTAINS(interests, '"Java"');
```

## Performance Tuning

### Slow Query Detection
```yaml
logging:
  level:
    org.springframework.jdbc.core: DEBUG
```

Then monitor system output for slow queries.

### Connection Pool Sizing
```
Recommended size = (number of cores × 2) + minimum_idle
Example: 4 cores → pool size of 10-15
```

### Index Strategy
```sql
-- Add for frequent searches
CREATE INDEX idx_city ON users(city);
CREATE INDEX idx_age ON users(age);

-- For OR queries
CREATE INDEX idx_name_company ON users(name, company);
```

## Troubleshooting Configuration

### "No DataSource found"
- Ensure `spring.datasource.url` is configured
- Verify `app.persistence.type: rdbms` is set
- Check database driver is in classpath

### "Cannot acquire JDBC Connection"
- Verify database is running
- Check connection URL format
- Verify username/password
- Confirm firewall allows connection

### "Table doesn't exist"
- Ensure `schema.sql` has been executed
- Check auto-create is enabled (it is by default)
- Manually run: `mysql < src/main/resources/schema.sql`

### "Too many connections"
- Increase `hikari.maximum-pool-size`
- Decrease idle timeout
- Check for connection leaks

## Example Configurations

### Development Setup
```yaml
app:
  persistence:
    type: rdbms

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC
    username: root
    password: UserSub@123
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  hikari:
    maximum-pool-size: 5
    minimum-idle: 1

logging:
  level:
    com.agentic.subscription: DEBUG
```

### Production Setup
```yaml
app:
  persistence:
    type: rdbms

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  hikari:
    maximum-pool-size: 20
    minimum-idle: 5
    connection-timeout: 30000

logging:
  level:
    root: WARN
    com.agentic.subscription: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,metrics
```

---

For more details, see:
- [RDBMS_MIGRATION.md](./RDBMS_MIGRATION.md)
- [QUICKSTART_RDBMS.md](./QUICKSTART_RDBMS.md)
