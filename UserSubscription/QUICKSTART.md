# Quick Start Guide - UserSubscription Service

Get the UserSubscription REST service up and running in 5 minutes!

## Prerequisites (First Time Setup)

```bash
# 1. Verify Java 17 is installed
java -version

# 2. Verify Maven is installed
mvn -version

# If not installed, use Homebrew:
# brew install openjdk@17
# brew install maven
```

## Start Service (Local - In-Memory)

```bash
# 1. Navigate to project directory
cd /Users/varunkashyap/AgenticCoding/UserSubscription

# 2. Build the project (first time only)
mvn clean install

# 3. Run the application
mvn spring-boot:run

# You should see:
# Started UserSubscriptionApplication in X seconds
# Server running on: http://localhost:8080
```

## Test the API (In Another Terminal)

```bash
# 1. Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "age": 28,
    "city": "San Francisco",
    "company": "CloudTech Inc",
    "interests": ["AI", "Machine Learning", "Cloud"]
  }'

# Response: Returns created user with ID
# Save the ID from response (e.g., "550e8400-e29b-41d4-a716-446655440000")

# 2. List all users
curl -X GET http://localhost:8080/api/v1/users

# 3. Get specific user (replace USER_ID)
curl -X GET http://localhost:8080/api/v1/users/USER_ID

# 4. Update user (replace USER_ID)
curl -X PUT http://localhost:8080/api/v1/users/USER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Updated",
    "age": 29,
    "city": "Seattle",
    "company": "TechFlow Labs",
    "interests": ["AI", "Blockchain", "Web3"]
  }'

# 5. Delete user (replace USER_ID)
curl -X DELETE http://localhost:8080/api/v1/users/USER_ID

# 6. Get user count
curl -X GET http://localhost:8080/api/v1/users/stats/count

# 7. Health check
curl -X GET http://localhost:8080/api/v1/users/health
```

## Run Tests

```bash
# Run all tests (in project directory)
mvn test

# Output shows:
# Tests run: 26
# Successes: 26
# Failures: 0
```

## Switch to OCI NoSQL

### Prerequisites
- OCI Account
- OCI CLI configured
- OCI NoSQL table created

### Steps

```bash
# 1. Update configuration
# Edit: src/main/resources/application.yml
# Change:
#   app:
#     persistence:
#       type: oci-nosql  # Change from 'in-memory' to 'oci-nosql'

# 2. Rebuild
mvn clean install

# 3. Run
mvn spring-boot:run

# Application will now use OCI NoSQL instead of in-memory storage
```

## Common Commands

```bash
# Clean build
mvn clean

# Build only (skip tests)
mvn package -DskipTests

# Run tests with coverage
mvn test jacoco:report && open target/site/jacoco/index.html

# View logs
mvn spring-boot:run -X

# Kill service (if running in background)
lsof -i :8080
kill -9 <PID>

# Check if service is running
curl -X GET http://localhost:8080/api/v1/users/health
```

## Project Structure

```
UserSubscription/
├── pom.xml              # Dependencies and build config
├── README.md            # Full documentation
├── IMPLEMENTATION.md    # Architecture details
├── QUICKSTART.md        # This file
├── src/main/java/       # Source code
├── src/main/resources/  # Configuration files (application.yml)
└── src/test/java/       # Test code
```

## API Endpoints Summary

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Create User | POST | /api/v1/users |
| List All | GET | /api/v1/users |
| Get by ID | GET | /api/v1/users/{id} |
| Update | PUT | /api/v1/users/{id} |
| Delete | DELETE | /api/v1/users/{id} |
| Count | GET | /api/v1/users/stats/count |
| Health | GET | /api/v1/users/health |

## Examples with Postman

### 1. Import Collection

1. Open Postman
2. Click "Import"
3. Create these requests:

### 2. Create User Request

```
POST http://localhost:8080/api/v1/users
Headers: Content-Type: application/json
Body:
{
  "name": "John Doe",
  "age": 30,
  "city": "Austin",
  "company": "Tech Corp",
  "interests": ["Java", "Cloud Computing", "Microservices"]
}
```

### 3. List Users Request

```
GET http://localhost:8080/api/v1/users
Headers: Content-Type: application/json
```

### 4. Get User Request

```
GET http://localhost:8080/api/v1/users/{{USER_ID}}
Headers: Content-Type: application/json
```

### 5. Update User Request

```
PUT http://localhost:8080/api/v1/users/{{USER_ID}}
Headers: Content-Type: application/json
Body:
{
  "name": "John Updated",
  "age": 31,
  "city": "New York",
  "company": "New Corp",
  "interests": ["Python", "AI"]
}
```

### 6. Delete User Request

```
DELETE http://localhost:8080/api/v1/users/{{USER_ID}}
Headers: Content-Type: application/json
```

## Troubleshooting

### Port 8080 Already In Use

```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>

# Or run on different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Java Version Issue

```bash
# Check Java version
java -version

# Should be Java 17 or higher
# If not installed

# Option 1: Homebrew (macOS)
brew install openjdk@17
export JAVA_HOME=/usr/local/opt/openjdk@17

# Option 2: Manual
# Download from https://adoptium.net/
# Extract and set JAVA_HOME
```

### Maven Not Found

```bash
# Install Maven
brew install maven

# Verify
mvn -version
```

### Tests Failing

```bash
# Run with verbose output
mvn test -X

# Check if service is running on different port
# Rebuild from scratch
mvn clean install

# Run specific test
mvn test -Dtest=UserServiceTest
```

## Next Steps

1. **Explore Code**
   - Check `src/main/java` for source code
   - Review `UserController.java` for REST endpoints
   - Look at `UserService.java` for business logic
   - Check `UserDAO.java` interface for abstraction

2. **Add Features**
   - Add filtering/search (update `listAllUsers`)
   - Add pagination (update list endpoint)
   - Add authentication (update controller)

3. **Connect to OCI**
   - Follow full README.md for OCI setup
   - Create NoSQL table in OCI
   - Update configuration
   - Deploy to cloud

4. **Deploy**
   - Build Docker image
   - Deploy to Kubernetes
   - Set up CI/CD pipeline

## Documentation

- **README.md** - Complete guide with detailed sections
- **IMPLEMENTATION.md** - Architecture and design decisions
- **QUICKSTART.md** - This file, get running in 5 minutes

## Support

For detailed information, see:
- README.md for comprehensive documentation
- IMPLEMENTATION.md for architecture details
- Code comments in source files for specific implementations

---

**Happy Coding!**

Start the service with `mvn spring-boot:run` and test with the curl commands above.
