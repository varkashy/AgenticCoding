# UserSubscription REST Service

A Spring Boot REST service for managing user subscriptions with flexible persistence layer supporting both RDBMS (MySQL, PostgreSQL, Oracle) and in-memory storage. The service provides a clean architecture with a DAO interface layer that allows for easy swapping of persistence implementations.

## 📢 Latest Updates

✅ **Phase 3 Complete** - Application fully enhanced with comprehensive documentation, testing, and API integration  
✅ **Swagger/OpenAPI Integrated** - Interactive API documentation at `/swagger-ui.html`  
✅ **E2E Testing Suite** - 9 comprehensive test methods with TestContainers MySQL  
✅ **JavaDoc Complete** - All classes fully documented with examples  
✅ **OCI Autonomous Database Ready** - Configuration and guide included  

**See [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md) for complete overview of all changes.**

## 📚 Quick Links to Documentation

| Document | Purpose | Time |
|----------|---------|------|
| **[IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)** | Overview of all enhancements ⭐ START HERE | 10 min |
| **[DOCUMENTATION_INDEX.md](./DOCUMENTATION_INDEX.md)** | Index of all documentation | Quick lookup |
| **[SWAGGER_GUIDE.md](./SWAGGER_GUIDE.md)** | Interactive API testing & documentation | 5 min |
| **[TESTING_GUIDE.md](./TESTING_GUIDE.md)** | Complete test suite guide | 10 min |
| **[AUTONOMOUS_DB_GUIDE.md](./AUTONOMOUS_DB_GUIDE.md)** | OCI database setup & deployment | 15 min |
| **[QUICKSTART_RDBMS.md](./QUICKSTART_RDBMS.md)** | 5-minute quick start | 5 min |
| **[MIGRATION_SUMMARY.md](./MIGRATION_SUMMARY.md)** | NoSQL → RDBMS migration details | 10 min |

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Running the Service](#running-the-service)
- [API Endpoints](#api-endpoints)
- [API Documentation](#api-documentation)
- [Database Setup](#database-setup)
- [Testing](#testing)
- [Local Development](#local-development)
- [Switching Persistence Types](#switching-persistence-types)
- [Migration from NoSQL](#migration-from-nosql)
- [Troubleshooting](#troubleshooting)

## Features

- ✅ **RESTful API** - Complete CRUD operations for user management
- ✅ **Flexible Persistence** - Support for RDBMS (MySQL, PostgreSQL) and in-memory storage
- ✅ **Automatic Database Initialization** - Tables created on startup if they don't exist
- ✅ **DAO Pattern** - Abstract data access layer for easy implementation swapping
- ✅ **Comprehensive Testing** - Unit and integration tests included
- ✅ **Spring Boot** - Modern Java framework with auto-configuration
- ✅ **Cloud-Ready** - Optimized for OCI, Azure, or any cloud provider
- ✅ **Validation** - Input validation with meaningful error messages
- ✅ **Logging** - Detailed logging for debugging and monitoring
- ✅ **Connection Pooling** - HikariCP for optimal database performance

## Architecture

```
UserSubscription Service
├── Controller Layer (REST endpoints)
│   └── UserController
├── Service Layer (Business logic)
│   └── UserService
├── DAO Layer (Data abstraction)
│   ├── UserDAO (Interface)
│   ├── RdbmsUserDAO (Implementation) - NEW
│   └── InMemoryUserDAO (Implementation)
├── Model Layer (Data entities)
│   └── User
└── Config Layer (Spring configuration)
    └── DatabaseConfiguration - NEW
```

## Quick Start

```bash
# 1. Clone and build
cd UserSubscription
mvn clean package -DskipTests

# 2. Run with in-memory database (no setup needed)
java -jar target/user-subscription-1.0.0.jar \
  --app.persistence.type=in-memory

# 3. Or run with local MySQL
# Prerequisites: MySQL running on localhost:3306
java -jar target/user-subscription-1.0.0.jar \
  --spring.profiles.active=local

# 4. Test the API
curl http://localhost:8080/api/v1/users
```

See [QUICKSTART_RDBMS.md](./QUICKSTART_RDBMS.md) for detailed setup guides.

## Prerequisites

- **Java 17+** - Required for Spring Boot 3.1.0 (Java 21 recommended for latest features)
- **Maven 3.8+** - Build tool
- **MySQL 8.0+** OR **PostgreSQL** (for RDBMS persistence)
- **Git** - Version control

### Installation Steps

### 1. Install Java 17 (or Java 21 for LTS)

```bash
# Using Homebrew (macOS) - Java 17
brew install openjdk@17

# Or Java 21 LTS (Recommended)
brew install openjdk@21

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Verify installation
java -version
```

### 2. Install Maven

```bash
# Using Homebrew (macOS)
brew install maven

# Verify installation
mvn -version
```

### 3. Install MySQL (for RDBMS persistence)

```bash
# Using Homebrew (macOS)
brew install mysql@8.0

# Start the service
brew services start mysql@8.0

# Or use Docker
docker run -d -e MYSQL_ROOT_PASSWORD=UserSub@123 -e MYSQL_DATABASE=userdb -p 3306:3306 mysql:8.0
```

## Project Structure

```
UserSubscription/
├── pom.xml                                          # Maven configuration
├── README.md                                        # This file
├── RDBMS_MIGRATION.md                              # Migration guide (NEW)
├── QUICKSTART_RDBMS.md                             # Quick start guide (NEW)
├── CONFIG_REFERENCE.md                             # Configuration reference (NEW)
├── MIGRATION_SUMMARY.md                            # Migration summary (NEW)
├── src/
│   ├── main/
│   │   ├── java/com/agentic/subscription/
│   │   │   ├── UserSubscriptionApplication.java    # Main Spring Boot app
│   │   │   ├── controller/
│   │   │   │   └── UserController.java             # REST endpoints
│   │   │   ├── service/
│   │   │   │   └── UserService.java                # Business logic
│   │   │   ├── dao/
│   │   │   │   ├── UserDAO.java                    # DAO interface
│   │   │   │   ├── RdbmsUserDAO.java               # RDBMS implementation (NEW)
│   │   │   │   └── InMemoryUserDAO.java            # In-memory implementation
│   │   │   ├── model/
│   │   │   │   └── User.java                       # User entity
│   │   │   └── config/
│   │   │       └── DatabaseConfiguration.java      # Database beans config (NEW)
│   │   └── resources/
│   │       ├── application.yml                     # Default configuration
│   │       ├── application-local.yml               # Local dev config (NEW)
│   │       ├── application-oci.yml                 # OCI cloud config (NEW)
│   │       └── schema.sql                          # Database DDL script (NEW)
│   └── test/
│       └── java/com/agentic/subscription/
│           ├── UserServiceTest.java                # Service layer tests
│           ├── UserControllerIntegrationTest.java  # Controller tests
│           └── dao/
│               ├── InMemoryUserDAOTest.java        # In-memory DAO tests
│               └── RdbmsUserDAOTest.java           # RDBMS DAO tests (NEW)
```

## Installation & Setup

### Step 1: Clone or Navigate to Project

```bash
# Navigate to the project directory
cd /Users/varunkashyap/AgenticCoding/UserSubscription
```

### Step 2: Build the Project

```bash
# Clean build
mvn clean package -DskipTests

# If tests pass with Java 21
mvn clean package
```
# Build using Maven
mvn clean install

# This will:
# - Download dependencies
# - Compile the code
# - Run all tests
# - Package the application
```

### Step 3: Verify Build Success

```bash
# Check for target directory
ls -la target/

# You should see: user-subscription-1.0.0.jar
```

## Configuration

### Persistence Type Selection

The application supports flexible persistence configuration via the `app.persistence.type` property:

```yaml
app:
  persistence:
    type: rdbms  # Options: 'rdbms' or 'in-memory'
```

### RDBMS Configuration (Production)

**File: `src/main/resources/application.yml`**

```yaml
app:
  persistence:
    type: rdbms  # Uses RdbmsUserDAO

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC
    username: root
    password: ${DB_PASSWORD:UserSub@123}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  hikari:
    maximum-pool-size: 10
    minimum-idle: 5
    connection-timeout: 30000
```

### Local Development Configuration

**File: `src/main/resources/application-local.yml`**

Use this profile for local MySQL development:
```bash
java -jar app.jar --spring.profiles.active=local
```

### OCI Cloud Configuration

**File: `src/main/resources/application-oci.yml`**

Use this profile for OCI MySQL Database System:
```bash
export MYSQL_HOST=your-mysql-hostname
java -jar app.jar --spring.profiles.active=oci
```

### In-Memory Configuration (Testing)

No configuration needed, just use the command line:
```bash
java -jar app.jar --app.persistence.type=in-memory
```

For detailed configuration options, see [CONFIG_REFERENCE.md](./CONFIG_REFERENCE.md)

## Database Setup

### Quick Setup (Automated)

The application automatically creates the database schema on startup if the tables don't exist. Just configure the database connection and run!

### Manual Setup

If automatic initialization doesn't work, run the DDL manually:

```bash
# Using MySQL CLI
mysql -h localhost -u root -p userdb < src/main/resources/schema.sql

# Or import via MySQL client
mysql -h <host> -u <user> -p <database> < schema.sql
```

For detailed setup instructions, see [QUICKSTART_RDBMS.md](./QUICKSTART_RDBMS.md)

## Running the Service

### Method 1: Using Maven (Development)

```bash
# Run with in-memory persistence
mvn spring-boot:run -Dspring-boot.run.arguments="--app.persistence.type=in-memory"

# Run with local MySQL
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# Output should show:
# 2026-03-06 10:30:00 - Started UserSubscriptionApplication in X seconds
# Server running on: http://localhost:8080
```

### Method 2: Using JAR File

```bash
# Build (if not already done)
mvn clean package -DskipTests

# Run with in-memory persistence (no database needed)
java -jar target/user-subscription-1.0.0.jar \
  --app.persistence.type=in-memory

# Run with local MySQL
java -jar target/user-subscription-1.0.0.jar \
  --spring.profiles.active=local

# Run with OCI MySQL
java -Dspring.profiles.active=oci \
  -DSPRING_DATASOURCE_URL=jdbc:mysql://your-host:3306/userdb \
  -DSPRING_DATASOURCE_USERNAME=root \
  -DSPRING_DATASOURCE_PASSWORD=YourPassword \
  -jar target/user-subscription-1.0.0.jar
```

The service will start on `http://localhost:8080`

## API Endpoints

All endpoints work seamlessly with any persistence type:

### 1. Create User
```http
POST /api/v1/users
Content-Type: application/json

{
  "name": "John Doe",
  "age": 30,
  "city": "Austin",
  "company": "Tech Corp",
  "interests": ["Java", "Cloud Computing", "Microservices"]
}

Response: 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "age": 30,
  "city": "Austin",
  "company": "Tech Corp",
  "interests": ["Java", "Cloud Computing", "Microservices"],
  "createdAt": 1707916200000,
  "updatedAt": 1707916200000
}
```

### 2. List All Users
```http
GET /api/v1/users
Content-Type: application/json

Response: 200 OK
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "John Doe",
    "age": 30,
    "city": "Austin",
    "company": "Tech Corp",
    "interests": ["Java", "Cloud Computing"],
    "createdAt": 1707916200000,
    "updatedAt": 1707916200000
  },
  ...
]
```

### 3. Get User by ID
```http
GET /api/v1/users/{id}
Content-Type: application/json

Response: 200 OK
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "age": 30,
  "city": "Austin",
  "company": "Tech Corp",
  "interests": ["Java", "Cloud Computing"],
  "createdAt": 1707916200000,
  "updatedAt": 1707916200000
}

# If not found: 404 Not Found
```

### 4. Update User
```http
PUT /api/v1/users/{id}
Content-Type: application/json

{
  "name": "John Doe Updated",
  "age": 31,
  "city": "New York",
  "company": "New Tech Corp",
  "interests": ["Python", "AI", "Cloud"]
}

Response: 200 OK
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe Updated",
  "age": 31,
  "city": "New York",
  "company": "New Tech Corp",
  "interests": ["Python", "AI", "Cloud"],
  "createdAt": 1707916200000,
  "updatedAt": 1707916300000
}
```

### 5. Delete User
```http
DELETE /api/v1/users/{id}

Response: 204 No Content

# If not found: 404 Not Found
```

### 6. Get User Count
```http
GET /api/v1/users/stats/count

Response: 200 OK
{
  "count": 5
}
```

### 7. Health Check
```http
GET /api/v1/users/health

Response: 200 OK
{
  "status": "UP"
}
```

## Testing with cURL

### Create a User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "age": 28,
    "city": "San Francisco",
    "company": "CloudTech Inc",
    "interests": ["AI", "Machine Learning", "Data Science"]
  }'
```

### List All Users
```bash
curl -X GET http://localhost:8080/api/v1/users
```

### Get Specific User
```bash
curl -X GET http://localhost:8080/api/v1/users/<USER_ID>
```

### Update User
```bash
curl -X PUT http://localhost:8080/api/v1/users/<USER_ID> \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Updated",
    "age": 29,
    "city": "Seattle",
    "company": "TechFlow Labs",
    "interests": ["AI", "Blockchain"]
  }'
```

### Delete User
```bash
curl -X DELETE http://localhost:8080/api/v1/users/<USER_ID>
```

### Get Count
```bash
curl -X GET http://localhost:8080/api/v1/users/stats/count
```

## Local Testing

### 1. Start with In-Memory Mode

The application defaults to in-memory persistence, perfect for local development:

```bash
mvn spring-boot:run
```

This uses `InMemoryUserDAO` which stores data in a `ConcurrentHashMap`.

### 2. Test with cURL or Postman

Use the provided cURL examples above to test the API.

### 3. Verify Data Persistence

```bash
# Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User", "age": 25, "city": "Austin", "company": "Test Corp", "interests": ["Testing"]}'

# Get all users (should return the created user)
curl -X GET http://localhost:8080/api/v1/users
```

**Note:** In-memory data is lost when the application stops.

## Running Tests

### Run All Tests

```bash
mvn test

# Output shows:
# Tests run: X
# Success: X
# Failures: 0
```

### Run Specific Test Class

```bash
# Run service tests
mvn test -Dtest=UserServiceTest

# Run DAO tests
mvn test -Dtest=InMemoryUserDAOTest

# Run controller integration tests
mvn test -Dtest=UserControllerIntegrationTest
```

### Run Tests with Coverage

```bash
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Switching Between In-Memory and OCI NoSQL

### To Use In-Memory (Local Testing)

Edit `src/main/resources/application.yml`:

```yaml
app:
  persistence:
    type: in-memory
```

Then restart the application:

```bash
mvn spring-boot:run
```

### To Use OCI NoSQL (Production)

1. Ensure OCI CLI is configured:
   ```bash
   oci setup config
   ```

2. Create a NoSQL table (see section below)

3. Update `src/main/resources/application.yml`:
   ```yaml
   app:
     persistence:
       type: oci-nosql
     oci:
       compartment-id: ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q
   ```

4. Restart the application:
   ```bash
   mvn spring-boot:run
   ```

## OCI NoSQL Database Setup

### Prerequisites

- OCI Account with appropriate permissions
- OCI CLI configured
- Access to AgenticAI compartment

### Step 1: Verify OCI Configuration

```bash
# Check OCI CLI configuration
cat ~/.oci/config

# Verify compartment ID
oci iam compartment list
```

### Step 2: Create NoSQL Table

```bash
# Set variables
COMPARTMENT_ID="ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q"
TABLE_NAME="users"

# Create the table
oci nosql table create \
  --compartment-id $COMPARTMENT_ID \
  --name $TABLE_NAME \
  --ddl-statement "CREATE TABLE users (id STRING, name STRING, age INTEGER, city STRING, company STRING, interests ARRAY(STRING), createdAt LONG, updatedAt LONG, PRIMARY KEY (id))" \
  --table-limits "{\"maxReadUnits\": 10, \"maxWriteUnits\": 10, \"maxStorageInGigabytes\": 25}"
```

### Step 3: Wait for Table Creation

```bash
# Check table status
oci nosql table get --name $TABLE_NAME --compartment-id $COMPARTMENT_ID

# Wait until ACTIVE status shows
```

### Step 4: Update Application Configuration

Update `application.yml` with your compartment ID and region, then update `OciConfiguration.java` if needed to point to correct profile.

### Step 5: Start Application with OCI NoSQL

```bash
mvn spring-boot:run
```

## Troubleshooting

### Issue: "Cannot connect to OCI NoSQL"

**Solution:**
1. Verify OCI CLI is configured: `oci setup config`
2. Check `~/.oci/config` file exists
3. Ensure API key file exists: `~/.oci/oci_api_key.pem`
4. Verify file permissions: `chmod 600 ~/.oci/config`

### Issue: "Table not found" when using OCI NoSQL

**Solution:**
1. Verify table exists: `oci nosql table list --compartment-id <COMPARTMENT_ID>`
2. Create table using Step 2 from OCI NoSQL Database Setup
3. Ensure table is in ACTIVE state

### Issue: Tests are failing

**Solution:**
1. Run tests with verbose output: `mvn test -X`
2. Check logs in `target/surefire-reports/`
3. Ensure Java 17+ is installed: `java -version`

### Issue: Application won't start

**Solution:**
1. Check port 8080 is available: `lsof -i :8080`
2. Kill process if needed: `kill -9 <PID>`
3. Check logs for errors: See console output
4. Verify Maven build: `mvn clean install -DskipTests`

### Issue: "java.lang.UnsupportedClassVersionError"

**Solution:**
1. Ensure Java 17 is installed
2. Set JAVA_HOME: `export JAVA_HOME=/path/to/java17`
3. Verify: `java -version`

## Development Workflow

### Adding a New Field to User

1. Update `User.java` model
2. Update `application.yml` if OCI NoSQL schema needs change
3. Update tests in `UserServiceTest.java` and `InMemoryUserDAOTest.java`
4. Update REST controller if needed
5. Run tests: `mvn test`

### Switching DAO Implementation

The beauty of the DAO pattern is that switching implementations requires minimal changes:

1. Update `app.persistence.type` in `application.yml`
2. No code changes needed in Service or Controller layers
3. Tests can be run with both implementations

### Adding a New Endpoint

1. Add method to `UserController.java`
2. Add business logic to `UserService.java`
3. Add corresponding DAO method if needed
4. Write integration test in `UserControllerIntegrationTest.java`
5. Run tests: `mvn test`

## Project Dependencies

- **Spring Boot 3.1.0** - Application framework
- **OCI Java SDK 2.57.0** - For OCI integration
- **Jackson** - JSON processing
- **Lombok** - Reduces boilerplate code
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Spring Test** - Integration testing

## Performance Considerations

### In-Memory Mode
- Fast response times
- No I/O latency
- Data lost on restart
- Suitable for development and testing

### OCI NoSQL Mode
- Persistent storage
- Network latency introduced
- Scalable to millions of records
- Production-ready
- Costs based on read/write units

## Security Considerations

### For Production

1. Add Spring Security for authentication/authorization
2. Use HTTPS instead of HTTP
3. Implement API key rotation for OCI
4. Add input validation and sanitization
5. Implement rate limiting
6. Use environment variables for sensitive config
7. Enable audit logging

### For Local Development

Current setup is suitable for development. No authentication required.

## Future Enhancements

1. Add pagination to list endpoint
2. Add search and filtering capabilities
3. Add user roles and permissions
4. Add audit trails
5. Add batch operations (bulk insert, update, delete)
6. Add caching layer
7. Add metrics and monitoring
8. Add API documentation (Swagger/OpenAPI)
9. Add GraphQL endpoint
10. Add event streaming/notifications

## Contributing

When making changes:
1. Follow the existing code structure
2. Write tests for new features
3. Run `mvn clean test` before committing
4. Update documentation
5. Keep logging consistent

## License

This project is provided as-is for educational and development purposes.

## Support

For issues or questions:
1. Check Troubleshooting section
2. Review application logs
3. Verify OCI configuration
4. Check test output for clues

---

**Last Updated:** February 14, 2026
**Version:** 1.0.0
