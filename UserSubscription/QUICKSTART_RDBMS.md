# Quick Start Guide - RDBMS Setup

## Step 1: Choose Your Database (Development or Production)

### Option A: Local MySQL (Development - No OCI needed)

#### Using Homebrew (macOS):
```bash
# Install MySQL 8.0
brew install mysql@8.0

# Start MySQL service
brew services start mysql@8.0

# Verify it's running
mysql -u root -p -e "SELECT 1"

# Create application database
mysql -u root -p << EOF
CREATE DATABASE IF NOT EXISTS userdb;
CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED BY 'UserSub@123';
GRANT ALL PRIVILEGES ON userdb.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
EOF
```

#### Using Docker:
```bash
docker run -d \
  --name user-subscription-mysql \
  -e MYSQL_ROOT_PASSWORD=UserSub@123 \
  -e MYSQL_DATABASE=userdb \
  -p 3306:3306 \
  mysql:8.0.33
```

### Option B: OCI MySQL Database System (Production - Cloud)

Using OCI Console:
1. Navigate to MySQL Database Service
2. Click "Create MySQL DB System"
3. Configure:
   - Display Name: `user-subscription-db`
   - MySQL Version: `8.0.32`
   - Shape: `MySQL.VM.Standard.E4.1.32GB`
   - Subnet: Select your VCN subnet
   - Admin Password: Set a strong password
4. Note the hostname when complete

Using OCI CLI:
```bash
# First, ensure you have network prerequisites
# 1. Create VCN (if not exists)
# 2. Create subnet
# 3. Create security list

COMPARTMENT_ID="ocid1.compartment.oc1..your_compartment_id"
SUBNET_ID="ocid1.subnet.oc1..your_subnet_id"

oci mysql db-system create \
  --compartment-id "$COMPARTMENT_ID" \
  --subnet-id "$SUBNET_ID" \
  --display-name user-subscription-db \
  --shape-name MySQL.VM.Standard.E4.1.32GB \
  --admin-username root \
  --admin-password 'YourStrongPassword123!' \
  --mysql-version 8.0.32 \
  --region us-ashburn-1
```

## Step 2: Build the Application

```bash
cd UserSubscription
mvn clean package -DskipTests
```

Output: `target/user-subscription-1.0.0.jar`

## Step 3: Run the Application

### Local Development:
```bash
java -jar target/user-subscription-1.0.0.jar \
  --spring.profiles.active=local \
  --spring.datasource.url=jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=UserSub@123
```

### OCI Deployment:
```bash
java -jar target/user-subscription-1.0.0.jar \
  --spring.profiles.active=oci \
  --spring.datasource.url=jdbc:mysql://your-mysql-host:3306/userdb?useSSL=true&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=YourPassword123!
```

### Docker:
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=oci \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/userdb \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=UserSub@123 \
  -v ~/.oci:/root/.oci:ro \
  user-subscription:1.0.0
```

## Step 4: Verify It Works

```bash
# Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "age": 30,
    "city": "New York",
    "company": "Tech Corp",
    "interests": ["Java", "Spring", "Docker"]
  }'

# List all users
curl http://localhost:8080/api/v1/users

# Get user by ID
curl http://localhost:8080/api/v1/users/{id}

# Health check
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Connection Refused
```bash
# Check if MySQL is running
sudo systemctl status mysql  # Linux
brew services list          # macOS
docker ps                   # Docker

# Test connection
mysql -h localhost -u root -p
```

### "Cannot authenticate user"
```bash
# Reset root password (Homebrew macOS)
mysql -u root < /dev/null

# Or use Docker with correct password
docker logs user-subscription-mysql
```

### "Unknown database 'userdb'"
```bash
# Create database manually
mysql -u root -p -e "CREATE DATABASE userdb;"
```

### RDBMS Tables Not Created
```bash
# Manually run schema
mysql -h localhost -u root -p userdb < src/main/resources/schema.sql

# Or verify auto-init settings in application.yml
cat src/main/resources/application.yml | grep "persistence"
```

## Environment Variables for Easy Configuration

```bash
# Set these once, use for all deployments
export DB_HOST="localhost"
export DB_PORT="3306"
export DB_NAME="userdb"
export DB_USER="root"
export DB_PASSWORD="UserSub@123"

# Then run with profile
java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=local
```

## Production Checklist

- [ ] Database created in OCI
- [ ] Security groups configured (allow port 3306 from application)
- [ ] Backup policy enabled
- [ ] Connection pooling configured (HikariCP settings)
- [ ] Connection timeout set appropriately
- [ ] Logging level set to INFO (not DEBUG)
- [ ] Health check configured
- [ ] Performance monitoring enabled
- [ ] Database user with limited privileges created
- [ ] Application deployed and verified

---

For detailed configuration options, see: [RDBMS_MIGRATION.md](./RDBMS_MIGRATION.md)
