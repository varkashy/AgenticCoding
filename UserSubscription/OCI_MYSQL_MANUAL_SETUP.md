# OCI MySQL Setup - Manual Guide

## Quick Summary
Follow these steps to create an OCI MySQL Database System through the Console. The process takes approximately 15 minutes.

---

## Step 1: Access OCI Console

1. Go to **https://cloud.oracle.com**
2. Log in with your OCI credentials
3. Ensure you're in the **US Ashburn** region (top-right corner)

---

## Step 2: Create a Virtual Cloud Network (VCN)

If you don't already have a VCN:

1. Click **Navigation Menu** (☰) → **Networking** → **Virtual Cloud Networks**
2. Click **Create VCN**
3. Fill in:
   - **Name**: `user-subscription-vcn`
   - **CIDR Block**: `10.0.0.0/16`
   - **Create Internet Gateway**: ✓ (checked)
   - **Create NAT Gateway**: ✓ (optional, for outbound traffic)
4. Click **Create VCN**
5. Wait for creation to complete (~1 minute)

---

## Step 3: Create a Subnet

1. In the VCN details, click **Subnets** (left sidebar)
2. Click **Create Subnet**
3. Fill in:
   - **Name**: `user-subscription-subnet`
   - **CIDR Block**: `10.0.1.0/24`
   - **Route Table**: Select the main route table
   - **Security Lists**: Select "Default Security List"
4. Click **Create Subnet**
5. Wait for creation (~30 seconds)

---

## Step 4: Create MySQL Database System

1. Click **Navigation Menu** (☰) → **Database** → **MySQL Database Service**
2. Click **Create MySQL DB System**
3. Fill in the form:

   **Basic Information:**
   - Name: `user-subscription-db`
   - Description: `User Subscription Application Database`
   - Select Compartment: Your compartment

   **MySQL Configuration:**
   - MySQL Version: `8.0.32`
   - Shape: `MySQL.VM.Standard.E4.1.32GB` (8 cores, 32 GB RAM)

   **Create Administrator Account:**
   - Username: `root`
   - Password: `UserSubscription@123`
   - Confirm Password: `UserSubscription@123`

   **Networking:**
   - Virtual Cloud Network: `user-subscription-vcn`
   - Subnet: `user-subscription-subnet`
   - Availability Domain: Select any available AD

   **Backup:**
   - Enable Backup: ✓ (checked)
   - Backup Retention Days: `7`

4. Review the configuration
5. Click **Create**
6. **Wait 10-15 minutes** for the instance to initialize

---

## Step 5: Get Connection Details

Once the MySQL instance is **ACTIVE**:

1. Click on `user-subscription-db` in the MySQL DB Systems list
2. Note the **MySQL Endpoint Hostname** (something like: `user-subscription-db.mysql.oraclecloud.com`)
3. Port: `3306` (default)
4. Username: `root`
5. Password: `UserSubscription@123`

---

## Step 6: Configure Application

Update your application configuration file:

**File: `src/main/resources/application-oci.yml`**

```yaml
spring:
  datasource:
    url: jdbc:mysql://user-subscription-db.mysql.oraclecloud.com:3306/userdb?useSSL=true&serverTimezone=UTC
    username: root
    password: UserSubscription@123
    driver-class-name: com.mysql.cj.jdbc.Driver
```

Or set environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://user-subscription-db.mysql.oraclecloud.com:3306/userdb?useSSL=true
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=UserSubscription@123
export SPRING_PROFILES_ACTIVE=oci
```

---

## Step 7: Configure Network Security (Important)

To allow your application to connect to MySQL:

1. Navigate to **Networking** → **Virtual Cloud Networks** → Your VCN
2. Click **Security Lists** → **Default Security List**
3. Click **Add Ingress Rule**
4. Add:
   - **Stateless**: No
   - **Source**: `0.0.0.0/0` (or your app's IP/CIDR)
   - **Destination Protocol**: TCP
   - **Destination Port Range**: `3306`
   - **Description**: `Allow MySQL connections`
5. Click **Add Ingress Rule**

---

## Step 8: Create Database

The `userdb` database will be created automatically when the application starts. However, you can create it manually if needed:

```bash
# Connect to MySQL
mysql -h user-subscription-db.mysql.oraclecloud.com -u root -p

# Create database (if not auto-created)
CREATE DATABASE IF NOT EXISTS userdb;

# Exit
EXIT;
```

---

## Step 9: Run Application

```bash
# Option 1: Using environment variables
export SPRING_DATASOURCE_URL=jdbc:mysql://user-subscription-db.mysql.oraclecloud.com:3306/userdb?useSSL=true
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=UserSubscription@123

java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=oci

# Option 2: Using command line
java -jar target/user-subscription-1.0.0.jar \
  --spring.profiles.active=oci \
  --spring.datasource.url=jdbc:mysql://user-subscription-db.mysql.oraclecloud.com:3306/userdb?useSSL=true \
  --spring.datasource.username=root \
  --spring.datasource.password=UserSubscription@123
```

The application will:
- Start on port 8080
- Automatically create the `users` table
- Be ready to receive requests

---

## Verification

Test the application:

```bash
# Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "age": 30,
    "city": "New York",
    "company": "Acme Corp",
    "interests": ["Java", "Cloud"]
  }'

# List all users
curl http://localhost:8080/api/v1/users

# Health check
curl http://localhost:8080/actuator/health
```

---

## Cost Estimation

The `MySQL.VM.Standard.E4.1.32GB` shape costs approximately:
- **~$1.00/hour** (pay-as-you-go)
- **~$730/month** if running 24/7

To reduce costs:
- Use `MySQL.VM.Standard.E2.1.8GB` (8 GB RAM, ~$0.25/hour)
- Stop when not in use
- Delete after testing

---

## Troubleshooting

### Connection Refused
- Check security list allows port 3306
- Verify MySQL endpoint hostname is correct
- Check firewall rules

### Authentication Failed
- Verify username and password
- Check special characters in password

### Slow Queries
- Check HikariCP pool size in `application.yml`
- Monitor query performance

### Can't Connect from Docker Container
- Use `host.docker.internal` instead of `localhost`
- Or expose MySQL port in Docker network

---

## Next Steps

Once MySQL is running:

1. **Deploy with Docker**:
   ```bash
   docker build -t user-subscription .
   docker run -e SPRING_PROFILES_ACTIVE=oci \
     -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/userdb \
     -p 8080:8080 user-subscription
   ```

2. **Deploy on OCI Container Instances**
3. **Set up CI/CD** with GitHub Actions
4. **Monitor** with OCI Monitoring and Logging

---

For more details, see:
- [QUICKSTART_RDBMS.md](./QUICKSTART_RDBMS.md)
- [CONFIG_REFERENCE.md](./CONFIG_REFERENCE.md)
- [RDBMS_MIGRATION.md](./RDBMS_MIGRATION.md)
