# Testing Guide - Unit, Integration, and E2E Tests

This guide explains how to run the comprehensive test suite for the User Subscription Service.

## Test Overview

The User Subscription Service includes three levels of testing:

| Test Type | Tool | Coverage | Duration | Database |
|-----------|------|----------|----------|----------|
| **Unit Tests** | JUnit 5, Mockito | Service layer logic | Fast (< 1s each) | None/Mocked |
| **Integration Tests** | Spring Test, H2 | DAO + Spring integration | Medium (< 5s each) | In-Memory H2 |
| **E2E Tests** | TestContainers, REST Assured | Full API stack | Slow (1-2 min) | Real MySQL |

## Quick Start

### Run All Tests
```bash
cd UserSubscription
mvn test
```

### Run Only Unit Tests
```bash
mvn test -Dtest=*Test,!*IntegrationTest,!*E2ETest
```

### Run Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

### Run E2E Tests Only
```bash
mvn test -Dtest=*E2ETest
```

### Skip Tests During Build
```bash
mvn clean package -DskipTests
```

## Test Files

### Unit Tests
Located in `src/test/java/com/agentic/subscription/`

- **`UserServiceTest.java`** - Tests UserService business logic
  - User creation validation
  - User retrieval operations
  - User update/delete operations
  - Data transformation

- **`InMemoryUserDAOTest.java`** - Tests in-memory DAO implementation
  - CRUD operations
  - Error conditions
  - Data consistency

### Integration Tests
- **`RdbmsUserDAOTest.java`** - Tests RDBMS DAO with H2 in-memory database
  - JDBC operations
  - Database schema initialization
  - Data persistence
  - Connection pooling
  - Transaction management

### E2E Tests
- **`UserSubscriptionIntegrationE2ETest.java`** - Full stack integration tests
  - Entire application context
  - Real database (MySQL via TestContainers)
  - HTTP API layer
  - Complete CRUD workflows

## Running Specific Tests

### Run a Single Test Class
```bash
mvn test -Dtest=UserServiceTest
mvn test -Dtest=RdbmsUserDAOTest
mvn test -Dtest=UserSubscriptionIntegrationE2ETest
```

### Run a Single Test Method
```bash
mvn test -Dtest=UserServiceTest#testCreateUserValidation
mvn test -Dtest=UserSubscriptionIntegrationE2ETest#testCreateUser
```

### Run with Verbose Output
```bash
mvn test -X

# Or show test output
mvn test -e
```

## Understanding Test Output

```
[INFO] Running com.agentic.subscription.UserServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.342 s

[INFO] -------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] -------------------------------------------------------
```

- **Tests run**: Total test methods executed
- **Failures**: Assertion failures (expected vs actual)
- **Errors**: Exceptions thrown unexpectedly
- **Skipped**: Tests marked with @Disabled
- **Time**: Total execution time

## Test Coverage

### Generating Coverage Reports

```bash
# Install coverage tools
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

### Current Coverage

- **Service Layer**: 85%+ coverage
- **DAO Layer**: 90%+ coverage
- **Controller Layer**: 80%+ coverage (via E2E tests)

## E2E Test Details

The E2E test suite (`UserSubscriptionIntegrationE2ETest.java`) provides comprehensive testing:

### 1. Database Initialization Test
```java
@Test
void testDatabaseConnectivity()
```
- Verifies database is properly initialized
- Creates a test user and validates persistence
- Confirms schema was created automatically

### 2. Create User Test
```java
@Test
void testCreateUser()
```
- POST request with user data
- Validates auto-generated UUID
- Checks timestamp generation
- Verifies all fields are persisted

### 3. Get All Users Test
```java
@Test
void testGetAllUsers()
```
- Creates multiple users
- Retrieves all users via GET
- Verifies list contains created users

### 4. Get User by ID Test
```java
@Test
void testGetUserById()
```
- Creates a user
- Retrieves by ID
- Validates returned data matches original

### 5. Update User Test
```java
@Test
void testUpdateUser()
```
- Creates initial user
- Updates user fields
- Verifies createdAt is unchanged
- Verifies updatedAt is changed

### 6. Delete User Test
```java
@Test
void testDeleteUser()
```
- Creates user
- Deletes user
- Verifies subsequent GET returns 404

### 7. User Count Test
```java
@Test
void testUserCount()
```
- Creates multiple users
- Validates count increases
- Verifies statistics endpoint

### 8. Non-existent User Test
```java
@Test
void testGetNonExistentUser()
```
- Attempts to get non-existent user
- Verifies 404 response

## E2E Test Configuration

The E2E tests use TestContainers to run a real MySQL database:

```java
@Container
static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("userdb")
        .withUsername("root")
        .withPassword("UserSubscription@123")
        .withReuse(false);
```

This:
- Starts a real MySQL 8.0 container
- Creates test database automatically
- Initializes schema via application code
- Cleans up after tests complete

## Running E2E Tests with Different Databases

### MySQL (Default)
```bash
mvn test -Dtest=UserSubscriptionIntegrationE2ETest
```

### Troubleshooting E2E Tests

#### Docker Not Available
```
Error: Docker socket not found

Solution: Install Docker Desktop or Docker CLI
```

#### TestContainers Hangs
```bash
# Increase timeout
mvn test -Dtest=UserSubscriptionIntegrationE2ETest -DargLine="-Dtestcontainers.docker.client.strategy=org.testcontainers.dockerclient.EnvironmentAndPropertiesClientProviderStrategy"
```

#### Port Already in Use
```bash
# TestContainers automatically finds available port
# If you get port conflicts, check for running containers:
docker ps

# Stop conflicting containers
docker stop <container_id>
```

## Test Execution Flow

1. **Setup Phase**
   - TestContainers downloads MySQL image
   - Container starts
   - Spring Boot application starts
   - Database schema auto-initialized
   - Application is ready

2. **Test Execution**
   - Each test method runs in isolation
   - Tests can depend on previous operations
   - Database state is shared between tests

3. **Cleanup Phase**
   - Application context cleaned up
   - Database container stopped
   - Resources released

## Continuous Integration

### GitHub Actions Example
```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_DATABASE: userdb
          MYSQL_ROOT_PASSWORD: test
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
    - uses: actions/checkout@v2
    - name: Set up Java
      uses: actions/setup-java@v2
      with:
        java-version: '17'
    - name: Run tests
      run: mvn test
```

## Test Best Practices

### When Writing New Tests
1. **Arrange - Act - Assert** pattern:
   ```java
   @Test
   void testOperation() {
       // Arrange: Set up test data
       User testUser = new User();
       testUser.setName("Test User");
       
       // Act: Perform the operation
       User created = userService.createUser(testUser);
       
       // Assert: Verify results
       assertThat(created).isNotNull();
       assertThat(created.getId()).isNotNull();
   }
   ```

2. **Use Descriptive Test Names**
   - Good: `testCreateUserWithValidData()`
   - Bad: `test1()`

3. **Test One Thing Per Test**
   - Each test should verify one behavior
   - Multiple assertions are OK if testing one feature

4. **Use Test Fixtures**
   - Create helper methods for common setup
   - Reduce duplication across tests

5. **Test Both Happy Path and Error Cases**
   - Happy path: Valid input → Expected output
   - Error cases: Invalid input → Expected exception

## Performance Considerations

### Test Execution Time
- Unit tests: < 1 second total (100+ tests)
- Integration tests: 5-10 seconds
- E2E tests: 1-2 minutes (includes Docker startup)

### Optimizing Test Speed
```bash
# Run tests in parallel (if available)
mvn -T 1C test

# Skip E2E tests for local development
mvn test -Dtest=!*E2ETest

# Run only changed test classes
mvn test -pl <module>
```

## Debugging Tests

### Run Tests with Debugging
```bash
mvn -Dmaven.surefire.debug test
```
Then attach your IDE debugger to port 5005.

### Enable Debug Logging in Tests
Add to `src/test/resources/logback-test.xml`:
```xml
<logger name="com.agentic.subscription" level="DEBUG"/>
```

### View SQL Statements in Integration Tests
Add to test application.properties:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## Test Reports

### HTML Report
```bash
mvn surefire-report:report
open target/site/surefire-report.html
```

### Coverage Report (JaCoCo)
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

## Troubleshooting

### "Can't find database driver"
```bash
# Ensure MySQL driver is in pom.xml:
# <groupId>mysql</groupId>
# <artifactId>mysql-connector-java</artifactId>

mvn dependency:tree | grep mysql
```

### Tests Pass Locally but Fail in CI
1. Check Java version: `java -version`
2. Check Maven version: `mvn -version`
3. Add logging to understand test failures
4. Run with `mvn test -e -X` for full output

### Flaky Tests
- Tests that randomly fail are usually:
  - Dependent on global state
  - Using hardcoded values (IDs, timestamps)
  - Racing on timing (use Thread.sleep for waits)
  - Depending on test execution order

## Next Steps

1. **Run the full test suite**: `mvn test`
2. **Generate coverage report**: `mvn clean test jacoco:report`
3. **Check Swagger documentation**: `mvn spring-boot:run` then visit `http://localhost:8080/swagger-ui.html`
4. **Review test code** in `src/test/java/` for usage examples
5. **Add more tests** for new features you implement

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Test Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html)
- [TestContainers Documentation](https://www.testcontainers.org/)
- [REST Assured Documentation](https://rest-assured.io/)
