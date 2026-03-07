# User Subscription API

A comprehensive Spring Boot REST API service for managing user subscriptions with RDBMS database integration, including support for OCI Autonomous AI Database.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database](#database)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## Features

- **CRUD Operations**: Full Create, Read, Update, Delete functionality for users
- **Automatic Database Initialization**: Automatically creates database and tables on startup
- **Multi-Database Support**: Works with MySQL, PostgreSQL, and Oracle Autonomous AI Database
- **RESTful API**: Clean and intuitive REST endpoints with proper HTTP status codes
- **Swagger/OpenAPI Documentation**: Auto-generated API documentation accessible via Swagger UI
- **Comprehensive Testing**: End-to-end integration tests with CRUD operations
- **Error Handling**: Robust error handling with appropriate HTTP responses
- **Logging**: Detailed logging for debugging and monitoring
- **Transaction Management**: Proper transaction handling for data consistency
- **Security**: Support for SSL/TLS connections to databases

## Architecture

The application follows a layered architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                            │
│              (UserController - REST Endpoints)               │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                 Business Logic Layer                         │
│             (UserService - Business Rules)                  │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│              Data Access Layer (DAO)                         │
│    (RdbmsUserDAO - Database Operations with JDBC)           │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                Database Layer                               │
│    (MySQL, PostgreSQL, Oracle Autonomous Database)          │
└─────────────────────────────────────────────────────────────┘
```

## Technology Stack

- **Framework**: Spring Boot 3.1.0
- **Language**: Java 17+
- **Build Tool**: Maven
- **Databases**: MySQL 8.0, PostgreSQL 14, Oracle Autonomous AI Database
- **Data Access**: Spring JDBC with JdbcTemplate
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5, Spring Test, MockMvc, Testcontainers
- **Connection Pooling**: HikariCP
- **JSON Processing**: Jackson

## Prerequisites

### System Requirements

- Java Development Kit (JDK) 17 or higher
- Maven 3.6.0 or higher
- Database server (MySQL, PostgreSQL, or Oracle)

### For Local Development

- MySQL Server 8.0+ OR PostgreSQL 14+
- Optional: Docker for containerized database

### For OCI Deployment

- OCI Autonomous AI Database instance (free tier available)
- OCI CLI configured with appropriate credentials
- OCI Wallet for database authentication

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd UserSubscription
```

### 2. Build the Project

```bash
mvn clean install
```

This will compile the code, run tests, and create a JAR file in the `target/` directory.

### 3. Skip Tests (Optional)

If you want to build without running tests:

```bash
mvn clean install -DskipTests
```

## Configuration

### Local Development (MySQL)

#### 1. Install MySQL

**macOS**:
```bash
brew install mysql@8.0
brew services start mysql@8.0
```

**Linux**:
```bash
sudo apt-get install mysql-server
sudo systemctl start mysql
```

**Windows**: Download and run MySQL Installer from mysql.com

#### 2. Create Database and User (Optional)

```bash
mysql -u root -p

CREATE DATABASE IF NOT EXISTS userdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'root'@'localhost' IDENTIFIED BY 'UserSubscription@123';
GRANT ALL PRIVILEGES ON userdb.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

Note: The application automatically creates the database and tables if they don't exist.

#### 3. Update Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC
    username: root
    password: UserSubscription@123
```

#### 4. Run the Application

```bash
mvn spring-boot:run
```

### Using Docker for MySQL

```bash
docker run -d \
  --name mysql-userdb \
  -e MYSQL_ROOT_PASSWORD=UserSubscription@123 \
  -e MYSQL_DATABASE=userdb \
  -p 3306:3306 \
  mysql:8.0

# Wait for MySQL to start (about 10 seconds)
sleep 10

# Run the application
mvn spring-boot:run
```

### OCI Autonomous AI Database Deployment

#### 1. Create Autonomous AI Database Instance

Using OCI Console or CLI:

```bash
oci db autonomous-database create \
  --compartment-id <COMPARTMENT_ID> \
  --db-name usersubdb \
  --display-name "user-subscription-aidb" \
  --db-workload "OLTP" \
  --admin-password "UserSubscription@123" \
  --is-free-tier true
```

#### 2. Download OCI Wallet

From OCI Console:
1. Navigate to Database → Autonomous Data Warehouse/Database
2. Select your database
3. Click "Database Connection"
4. Download the wallet ZIP file

#### 3. Extract and Configure Wallet

```bash
mkdir -p /opt/oracle/tns
unzip Wallet_*.zip -d /opt/oracle/tns
chmod 600 /opt/oracle/tns/*
```

#### 4. Update Configuration

Create or use `src/main/resources/application-oci.yml`:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@usersubdb_high?TNS_ADMIN=/opt/oracle/tns
    username: admin
    password: ${DB_PASSWORD:UserSubscription@123}
    driver-class-name: oracle.jdbc.OracleDriver
```

#### 5. Run with OCI Profile

```bash
export SPRING_PROFILES_ACTIVE=oci
export DB_PASSWORD=YourDatabasePassword
mvn spring-boot:run
```

## Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Production JAR

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/user-subscription-1.0.0.jar
```

### With Environment Variables

```bash
java -jar target/user-subscription-1.0.0.jar \
  --spring.datasource.url=jdbc:mysql://db-host:3306/userdb \
  --spring.datasource.username=root \
  --spring.datasource.password=password123 \
  --server.port=8080
```

### Docker Deployment

#### 1. Build Docker Image

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/user-subscription-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 2. Run Container

```bash
docker build -t user-subscription:latest .

docker run -d \
  --name user-subscription \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/userdb \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=UserSubscription@123 \
  --link mysql-db:mysql-db \
  user-subscription:latest
```

## API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation:

```
http://localhost:8080/swagger-ui.html
```

### API Endpoints

All endpoints are prefixed with `/api/v1/users`

#### 1. Create User
```http
POST /api/v1/users
Content-Type: application/json

{
  "name": "John Doe",
  "age": 30,
  "city": "San Francisco",
  "company": "Tech Corp",
  "interests": ["coding", "reading", "travel"]
}

Response: 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "age": 30,
  "city": "San Francisco",
  "company": "Tech Corp",
  "interests": ["coding", "reading", "travel"],
  "createdAt": 1692432000000,
  "updatedAt": 1692432000000
}
```

#### 2. Get All Users
```http
GET /api/v1/users

Response: 200 OK
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "John Doe",
    ...
  }
]
```

#### 3. Get User by ID
```http
GET /api/v1/users/{id}

Response: 200 OK
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  ...
}
```

#### 4. Update User
```http
PUT /api/v1/users/{id}
Content-Type: application/json

{
  "name": "Jane Doe",
  "age": 31,
  "city": "Boston",
  "company": "New Corp",
  "interests": ["design", "hiking"]
}

Response: 200 OK
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Jane Doe",
  ...
}
```

#### 5. Delete User
```http
DELETE /api/v1/users/{id}

Response: 204 No Content
```

#### 6. Get User Count
```http
GET /api/v1/users/stats/count

Response: 200 OK
{
  "count": 5
}
```

#### 7. Health Check
```http
GET /api/v1/users/health

Response: 200 OK
{
  "status": "UP"
}
```

## Database

### Schema

The application automatically creates the following tables:

#### Users Table
```sql
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  age INT,
  city VARCHAR(255),
  company VARCHAR(255),
  interests JSON,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  INDEX idx_created_at (created_at),
  INDEX idx_name (name)
);
```

#### Audit Log Table (Optional)
```sql
CREATE TABLE audit_log (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  action VARCHAR(50) NOT NULL,
  timestamp BIGINT NOT NULL,
  INDEX idx_user_id (user_id),
  INDEX idx_timestamp (timestamp),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Automatic Initialization

On the first run, the application will:
1. Check if the database exists
2. Create the database if needed
3. Check if tables exist
4. Create tables with proper schema
5. Create indexes for performance

No manual database setup is required!

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=UserControllerE2ETest
```

### Run with Coverage

```bash
mvn test jacoco:report
# Open target/site/jacoco/index.html
```

### Test Categories

1. **Unit Tests**: Test individual components in isolation
   - `UserServiceTest.java`: Business logic tests
   - `RdbmsUserDAOTest.java`: DAO operations with H2

2. **Integration Tests**: Test component interactions
   - `UserControllerIntegrationTest.java`: API with database

3. **End-to-End Tests**: Complete workflow testing
   - `UserControllerE2ETest.java`: Full CRUD operations through REST API

### Test Coverage

The application includes comprehensive tests covering:
- ✅ User creation with automatic ID generation
- ✅ User retrieval by ID and listing all users
- ✅ User updates with timestamp tracking
- ✅ User deletion with cascade
- ✅ Error handling for invalid requests
- ✅ Edge cases (empty interests, null values)
- ✅ Database consistency
- ✅ HTTP status codes

## Troubleshooting

### Issue: Database Connection Failed

**Problem**: `Cannot create a new datasource`

**Solutions**:
1. Verify MySQL is running: `mysql --version`
2. Check credentials in `application.yml`
3. Ensure database user exists and has permissions
4. For OCI: Verify wallet is in correct location and has correct permissions

### Issue: Port 8080 Already in Use

**Solution**:
```bash
# Change port in application.yml
server:
  port: 8081

# Or via command line
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Issue: Tests Failing with H2 Database

**Problem**: `SQLException: Table not found`

**Solution**: H2 is in MySQL compatibility mode. Schema is auto-created. If it fails:
```bash
# Clean and rebuild
mvn clean test
```

### Issue: JAR Not Found After Build

**Solution**:
```bash
mvn clean install -DskipTests
# Check target/ directory
ls -la target/user-subscription-*.jar
```

### Debug Mode

Enable debug logging:
```bash
export LOGGING_LEVEL_COM_AGENTIC_SUBSCRIPTION=DEBUG
mvn spring-boot:run
```

Or in `application.yml`:
```yaml
logging:
  level:
    com.agentic.subscription: DEBUG
```

## JavaDoc and Code Documentation

The codebase includes comprehensive JavaDoc comments:

- **Controllers** (`UserController.java`): Each endpoint documented with purpose, parameters, returns, exceptions
- **Services** (`UserService.java`): Business logic methods with detailed documentation
- **DAOs** (`RdbmsUserDAO.java`): Database operation details including SQL handling
- **Models** (`User.java`): Entity structure and relationships
- **Configuration** (`DatabaseConfiguration.java`, `OciConfiguration.java`): Setup procedures and bean descriptions

To generate JavaDoc:

```bash
mvn javadoc:javadoc
# Generated documentation in target/site/apidocs/index.html
```

## Performance Optimization

### Connection Pooling (HikariCP)

The application uses HikariCP with:
- Maximum pool size: 10 (development), 20 (production/OCI)
- Minimum idle connections: 5
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes

### Database Indexes

- `idx_created_at`: Optimizes sorting and time-range queries
- `idx_name`: Optimizes full-text search if implemented

### Batch Operations

Consider using:
```java
List<User> users = getAllUsers();
// Process in batches for bulk operations
```

## Monitoring and Logging

### Health Check Endpoint

```bash
curl http://localhost:8080/api/v1/users/health
```

### Actuator Endpoints

```yaml
# In application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

Access at: `http://localhost:8080/actuator/health`

### Application Logs

Default log level: `INFO`

View debug logs:
```bash
tail -f logs/application.log | grep DEBUG
```

## Contributing

1. Ensure all tests pass: `mvn test`
2. Follow JavaDoc conventions for new methods
3. Update README for new features
4. Use meaningful commit messages

## License

Apache License 2.0

## Support

For issues or questions:
1. Check the Troubleshooting section
2. Review test cases for usage examples
3. Check Swagger UI documentation at `/swagger-ui.html`
4. Review JavaDoc comments in source code

---

**Last Updated**: March 2026  
**Version**: 1.0.0  
**Status**: Production Ready
