# RDBMS Migration Guide

This document describes the migration from OCI NoSQL database to a relational database (RDBMS).

## Overview

The application has been migrated from Oracle Cloud Infrastructure (OCI) NoSQL database to a traditional RDBMS (MySQL). The migration includes:

- **Renaming**: OciNoSqlUserDAO → RdbmsUserDAO
- **Config**: OciConfiguration → DatabaseConfiguration
- **Dependencies**: Removed OCI NoSQL SDK, added MySQL JDBC driver and Spring JDBC
- **Auto-initialization**: Database schema is automatically created on application startup if it doesn't exist
- **Multiple Profiles**: Support for local development and OCI cloud deployment

## Database Setup

### Option 1: OCI MySQL Database System (Production)

Create a MySQL Database System in your OCI compartment:

```bash
oci mysql db-system create \
  --compartment-id <your-compartment-id> \
  --display-name user-subscription-db \
  --subnet-id <your-subnet-id> \
  --admin-password '<strong-password>' \
  --shape-name MySQL.VM.Standard.E4.1.32GB \
  --mysql-version 8.0.32 \
  --region us-ashburn-1
```

After creation, note the MySQL DB System endpoint (hostname).

### Option 2: Local MySQL (Development)

#### macOS with Homebrew:
```bash
# Install MySQL
brew install mysql@8.0

# Start MySQL
brew services start mysql@8.0

# Create database and user
mysql -u root -p << 'EOF'
CREATE DATABASE userdb;
CREATE USER 'root'@'localhost' IDENTIFIED BY 'UserSub@123';
GRANT ALL PRIVILEGES ON userdb.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
EOF
```

#### Docker:
```bash
docker run --name mysql-user-subscription \
  -e MYSQL_ROOT_PASSWORD=UserSub@123 \
  -e MYSQL_DATABASE=userdb \
  -p 3306:3306 \
  -d mysql:8.0
```

### Option 3: Azure MySQL Database (Cloud)

Use Azure Portal or CLI to create a MySQL Server:

```bash
az mysql server create \
  --resource-group <your-rg> \
  --name user-subscription-db \
  --location eastus \
  --admin-user root \
  --admin-password <strong-password>
```

## Configuration

### Environment Variables

Configure the database connection using environment variables:

```bash
export SPRING_PROFILES_ACTIVE=local  # or 'oci' for cloud
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=UserSub@123
```

### Application Profiles

#### Local Development
Use `application-local.yml` (automatically selected when `SPRING_PROFILES_ACTIVE=local`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC
    username: root
    password: UserSub@123
```

#### OCI Deployment
Use `application-oci.yml` (select with `SPRING_PROFILES_ACTIVE=oci`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST}:3306/userdb?useSSL=true&serverTimezone=UTC
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
```

#### Default Configuration
`application.yml` is used when no profile is specified.

## Running the Application

### With Maven
```bash
# Local development with in-memory DB
mvn clean spring-boot:run -Dspring-boot.run.arguments="--app.persistence.type=in-memory"

# Local development with MySQL
mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# OCI deployment
mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=oci"
```

### Building JAR
```bash
mvn clean package

# Run with local MySQL
java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=local

# Run with OCI
java -Dspring.profiles.active=oci \
     -DMYSQL_HOST=<your-mysql-hostname> \
     -DMYSQL_USERNAME=root \
     -DMYSQL_PASSWORD=<password> \
     -jar target/user-subscription-1.0.0.jar
```

## Database Schema

The application automatically creates the following tables on startup:

### users table
- `id` (VARCHAR 36): Primary key, UUID
- `name` (VARCHAR 255): User name
- `age` (INT): User age
- `city` (VARCHAR 255): User city
- `company` (VARCHAR 255): User company
- `interests` (JSON): User interests array
- `created_at` (BIGINT): Creation timestamp
- `updated_at` (BIGINT): Last update timestamp

Indexes:
- `idx_created_at`: On creation timestamp
- `idx_name`: On user name
- `idx_company`: On company

### audit_log table (optional)
Tracks all changes to users:
- `id` (INT AUTO_INCREMENT): Primary key
- `user_id` (VARCHAR 36): References users.id
- `action` (VARCHAR 50): CREATE, UPDATE, DELETE
- `changed_fields` (JSON): Changed field details
- `changed_at` (BIGINT): Change timestamp

## Manual Schema Creation

If automatic initialization doesn't work, run the DDL manually:

```bash
mysql -h <host> -u <user> -p <database> < src/main/resources/schema.sql
```

## Persistence Types

The application supports multiple persistence implementations via Spring's conditional bean registration:

### In-Memory (Testing/Development)
```yaml
app:
  persistence:
    type: in-memory
```
Uses `InMemoryUserDAO` - no database required

### RDBMS (Production)
```yaml
app:
  persistence:
    type: rdbms
```
Uses `RdbmsUserDAO` - requires configured DataSource

## Entity Support

### Persistence in RDBMS:
- ✅ Users with all attributes
- ✅ Interests stored as JSON
- ✅ Timestamps as milliseconds (BIGINT)
- ✅ Full CRUD operations
- ✅ Query by ID, list all, search by name/company

## Migration from NoSQL

If migrating existing data from OCI NoSQL:

1. Export data from NoSQL to JSON
2. Transform to match RDBMS schema
3. Using the `/api/v1/users` endpoint with POST requests to populate:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "age": 30,
    "city": "New York",
    "company": "Acme Corp",
    "interests": ["Java", "Spring", "Cloud"]
  }'
```

## Troubleshooting

### Connection Issues
- Verify database is running: `mysql -u root -p -h 127.0.0.1`
- Check firewall rules for OCI MySQL: Allow port 3306 from application
- Verify SPRING_DATASOURCE_URL format

### Schema Creation Fails
- Ensure database exists: `CREATE DATABASE userdb;`
- Check user permissions: `GRANT ALL ON userdb.* TO 'root'@'%';`
- Run manual schema creation: `mysql ... < schema.sql`

### Performance Issues
- Adjust HikariCP settings in `application.yml`
- Consider adding more indexes for frequently queried fields
- Monitor with `management.endpoints.web.exposure.include: health,metrics`

## Testing

Run the test suite:

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=RdbmsUserDAOTest

# With coverage
mvn clean test jacoco:report
```

Common test configurations use `@DataJpaTest` or `@SpringBootTest` with embedded H2 database.

## Next Steps

1. Set up your OCI MySQL Database System or local MySQL
2. Configure database connection in `application.yml` or via environment variables
3. Build the application: `mvn clean package`
4. Run with appropriate profile: `java -jar ... --spring.profiles.active=oci`
5. Access API at `http://localhost:8080/api/v1/users`

## References

- [MySQL JDBC Driver Documentation](https://dev.mysql.com/doc/connector-j/8.0/en/)
- [Spring JDBC Documentation](https://spring.io/projects/spring-framework)
- [OCI MySQL Database Service](https://www.oracle.com/cloud/mysql/)
