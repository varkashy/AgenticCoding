# Autonomous AI Database Integration Guide

Complete guide to integrating the User Subscription Service with Oracle Cloud Infrastructure (OCI) Autonomous AI Database.

## Quick Reference

| Component | Value |
|-----------|-------|
| **Database Name** | usersubdb |
| **Database Type** | Oracle Autonomous AI Database |
| **Database Edition** | OCI Free Tier |
| **Admin User** | admin |
| **Default Password** | UserSubscription@123 |
| **Region** | us-ashburn-1 (change if needed) |
| **Workload Type** | OLTP (Online Transaction Processing) |
| **MySQL Version** | 8.0.32 |

## Prerequisites

### 1. OCI Account
- [Create Free OCI Account](https://www.oracle.com/cloud/free/)
- Get Free Tier resources (always free)
- Autonomous AI Database included in free tier

### 2. OCI CLI Installed
```bash
# Install OCI CLI
brew install oci-cli

# Verify installation
oci --version

# Configure credentials
oci setup config
```

### 3. Java and Maven
```bash
java -version  # Java 17 or higher
mvn -version   # Maven 3.8 or higher
```

## Connection Methods

### Method 1: Using Connection String (Recommended for Development)

This method uses the database hostname directly.

#### Step 1: Get Database Connection Details

1. Log in to [OCI Console](https://cloud.oracle.com)
2. Navigate to **Databases** → **Autonomous AI Database**
3. Click on **usersubdb** database
4. Under "Database Connection", find and copy:
   - Hostname (e.g., `usersubdb.c9akciq32xce.database.oraclecloud.com`)
   - Port (usually `3306`)
   - Database name (usually `usersubdb`)

#### Step 2: Build Connection String

```
jdbc:mysql://<hostname>:3306/usersubdb?useSSL=true&serverTimezone=UTC
```

Example:
```
jdbc:mysql://usersubdb.c9akciq32xce.database.oraclecloud.com:3306/usersubdb?useSSL=true&serverTimezone=UTC
```

#### Step 3: Run Application with OCI Profile

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://<hostname>:3306/usersubdb?useSSL=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="UserSubscription@123"
export SPRING_PROFILES_ACTIVE="oci"

cd UserSubscription
mvn spring-boot:run

# Or with JAR
java -jar target/user-subscription-1.0.0.jar
```

### Method 2: Using Oracle Wallet (Recommended for Production)

The Oracle Wallet provides encrypted connection configuration.

#### Step 1: Download Wallet

1. In OCI Console, go to **Autonomous Databases** → **usersubdb**
2. Click **Database Connection** button
3. Download the wallet (`.zip` file)
4. Extract to a secure location: `/path/to/wallet/`

#### Step 2: Configure Application

```bash
# Set wallet location
export TNS_ADMIN=/path/to/wallet

# Set connection
export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@usersubdb_high?TNS_ADMIN=/path/to/wallet"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="UserSubscription@123"
export DB_DRIVER="oracle.jdbc.OracleDriver"
export SPRING_PROFILES_ACTIVE="oci"

mvn spring-boot:run
```

### Method 3: Local Port Forwarding

For secure connections through SSH tunnel.

```bash
# Create SSH tunnel to database
ssh -L 3306:usersubdb.c9akciq32xce.database.oraclecloud.com:3306 \
    -i /path/to/private/key user@bastion-host

# Use localhost connection
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/usersubdb?useSSL=false"
mvn spring-boot:run
```

## Configuration Profiles

### Default Profile (Local MySQL)
```bash
mvn spring-boot:run
# Uses: jdbc:mysql://localhost:3306/userdb
```

### OCI Profile (Autonomous Database)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=oci"
# Uses: Environment variables for connection details
```

### Local Development Profile
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
# Uses: Local MySQL instance
```

## Database Structure

The application automatically creates the required tables on startup.

### Tables Created

#### `users` Table
Stores user subscription information.

```sql
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,           -- UUID v4
  name VARCHAR(255) NOT NULL,           -- User's name
  age INT,                              -- Optional age
  city VARCHAR(255),                    -- Optional city
  company VARCHAR(255),                 -- Optional company
  interests JSON,                       -- Array of interests stored as JSON
  created_at BIGINT NOT NULL,           -- Creation timestamp (ms)
  updated_at BIGINT NOT NULL,           -- Last update timestamp (ms)
  INDEX idx_created_at (created_at),
  INDEX idx_name (name)
);
```

#### `audit_log` Table
Optional audit trail for tracking changes (created on startup if needed).

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

**Note**: Tables are created automatically via `DatabaseConfiguration.java` on application startup.

## Verification Steps

### Step 1: Verify Database Connection

```bash
#!/bin/bash
# Run test script

# Set variables
DB_HOST="usersubdb.c9akciq32xce.database.oraclecloud.com"
DB_USER="admin"
DB_PASS="UserSubscription@123"
DB_NAME="usersubdb"

# Test connection with MySQL CLI
mysql -h $DB_HOST -u $DB_USER -p$DB_PASS $DB_NAME -e "SELECT 1;"

# Expected output: 1 row returned
```

### Step 2: Verify Tables Created

Once application starts, verify tables exist:

```bash
mysql -h $DB_HOST -u $DB_USER -p$DB_PASS $DB_NAME -e "SHOW TABLES;"

# Expected output:
# Tables_in_usersubdb
# users
# audit_log
```

### Step 3: Verify Application Started

```bash
# Check application health
curl http://localhost:8080/api/v1/users/health

# Expected: 
# {"status":"UP"}
```

### Step 4: Test API with Swagger

1. Navigate to: `http://localhost:8080/swagger-ui.html`
2. Create a test user via Swagger UI
3. Retrieve users to verify persistence

## Deployment to OCI Application Container Cloud

### Deploy with OCI DevOps

```bash
# Build JAR
mvn clean package

# Create OCI config
cat > .ocidevops.yml << 'EOF'
version: 0.1
shell: bash
env:
  variables:
    AWS_REGION: "us-ashburn-1"
    JAVA_VERSION: "17"

stages:
  - name: Build
    steps:
      - type: Command
        command: |
          mvn clean package -DskipTests
  
  - name: Docker Build
    steps:
      - type: DockerBuild
        image: user-subscription:latest

  - name: Push to OCI Registry
    steps:
      - type: Command
        command: |
          docker tag user-subscription:latest \
            <region>.ocir.io/<namespace>/user-subscription:latest
          docker push <region>.ocir.io/<namespace>/user-subscription:latest

  - name: Deploy to Container Instances
    steps:
      - type: Command
        command: |
          oci compute instance launch \
            --image-id <image-ocid> \
            --shape VM.Standard.E4.Flex
EOF
```

### Deploy with Docker

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/user-subscription-1.0.0.jar app.jar

ENV SPRING_PROFILES_ACTIVE=oci
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t user-subscription:1.0.0 .

docker run \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://<hostname>:3306/usersubdb?useSSL=true" \
  -e SPRING_DATASOURCE_USERNAME="admin" \
  -e SPRING_DATASOURCE_PASSWORD="UserSubscription@123" \
  -e SPRING_PROFILES_ACTIVE="oci" \
  -p 8080:8080 \
  user-subscription:1.0.0
```

## Performance Tuning

### Connection Pool Optimization

Edit `application-oci.yml`:

```yaml
spring:
  datasource:
    hikari:
      # Increase for high-traffic scenarios
      maximum-pool-size: 30
      minimum-idle: 10
      # Connection timeout (ms)
      connection-timeout: 30000
      # Idle timeout (ms) - 15 minutes
      idle-timeout: 900000
      # Max lifetime (ms) - 30 minutes
      max-lifetime: 1800000
      # Connection test query
      connection-test-query: "SELECT 1"
      # Auto-commit enabled for autonomous DB
      auto-commit: true
```

### Database-Level Optimizations

1. **Enable Query Result Caching**
   ```sql
   ALTER SYSTEM SET query_result_cache_size=0 SCOPE=BOTH;
   ```

2. **Optimize Indexes**
   ```sql
   CREATE INDEX idx_name_lower ON users(LOWER(name));
   CREATE INDEX idx_interests ON users((JSON_EXTRACT(interests, '$')));
   ```

3. **Monitor Performance**
   - Use OCI Console → Database Performance Hub
   - Monitor CPU, memory, storage usage
   - Analyze slow queries

## Backup and Recovery

### Automated Backups

OCI Autonomous Database provides automatic backups:
- **Full backups**: Daily
- **Retention**: 30 days by default
- **Recovery**: Point-in-time recovery available

### Manual Backup

```bash
# Export data
mysqldump -h <hostname> -u admin -p usersubdb > backup.sql

# Restore
mysql -h <hostname> -u admin -p usersubdb < backup.sql
```

## Monitoring and Troubleshooting

### View Database Metrics

In OCI Console:
1. Databases → Autonomous AI Database → usersubdb
2. Click **Metrics** tab
3. View CPU, Memory, Storage, Network usage

### Enable Detailed Logging

```yaml
# Add to application-oci.yml
logging:
  level:
    root: INFO
    com.agentic.subscription: DEBUG
    org.springframework.jdbc: DEBUG
    org.springframework.web: DEBUG
    com.mysql.cj: DEBUG
    oracle.jdbc: DEBUG
```

### Common Issues

#### Issue: "Can't connect to database"
```bash
# Check network connectivity
ping usersubdb.c9akciq32xce.database.oraclecloud.com

# Verify firewall rules allow port 3306
# Check OCI console → Network → Security Lists

# Test with mysql-cli
mysql -h <hostname> -u admin -p usersubdb -e "SELECT 1;"
```

#### Issue: "Connection timeout"
```properties
# Increase timeout in application-oci.yml
spring.datasource.hikari.connection-timeout=60000
```

#### Issue: "Can't auth root/user"
```bash
# Reset admin password in OCI Console:
# Databases → usersubdb → More Options → Reset Admin Password
```

## Security Best Practices

### 1. Use Network Security Groups

```bash
# Create NSG to restrict database access
oci network nsg create \
  --compartment-id <compartment-ocid> \
  --display-name database-nsg

# Allow only from app server
oci network nsg-rules create \
  --nsg-id <nsg-ocid> \
  --egress-security-rules '[
    {
      "isStateless": false,
      "protocol": "6",
      "destination": "10.0.0.0/16",
      "destinationPortRange": {
        "min": 3306,
        "max": 3306
      }
    }
  ]'
```

### 2. Enable Database Encryption

- Manual: Encrypt wallet file
- Automatic: OCI handles encryption at rest

### 3. Rotate Passwords Regularly

```bash
# Change admin password every 90 days
oci db autonomous-database update \
  --autonomous-database-id <db-ocid> \
  --admin-password <new-password>
```

### 4. Use Secrets Management

Store credentials in OCI Vault:

```bash
# Create secret
oci secrets secret create \
  --compartment-id <compartment-ocid> \
  --secret-name db-password \
  --secret-content "UserSubscription@123"

# Retrieve secret
oci secrets secret-bundle get \
  --secret-id <secret-ocid> \
  --query 'data."secret-bundle-content".content' \
  --raw-output
```

## Cost Optimization

### OCI Free Tier Limits
- ✅ 1 Always-Free Autonomous Database (20 GB)
- ✅ Covers this project usage
- ✅ Perpetual free tier (never expires)

### Minimize Costs
1. Use free tier resources
2. Enable idle database shutdown
3. Downsize compute before production
4. Enable auto-scaling during peak hours

## Next Steps

1. **Verify Connection**: Run test commands above
2. **Deploy Application**: Use Docker or OCI DevOps
3. **Monitor Performance**: Check OCI Dashboard
4. **Optimize Queries**: Review slow query logs
5. **Scale as Needed**: Increase VM compute shape

## Support & Resources

- [OCI Autonomous Database Documentation](https://docs.oracle.com/en/cloud/paas/autonomous-database/)
- [OCI Free Tier Details](https://www.oracle.com/cloud/free/)
- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/)
- [Application Documentation](./SWAGGER_GUIDE.md)
