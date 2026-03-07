# Testing Application Against OCI Autonomous Database

## Current Status

✅ **OCI Autonomous AI Database**: AVAILABLE  
✅ **Database Name**: user-subscription-aidb  
✅ **Region**: us-ashburn-1  
✅ **Admin**: admin / UserSubscription@123  

❓ **Missing**: Tables haven't been created yet (database is empty)

---

## Why There Are No Tables Yet

The application has **automatic table creation** that runs when it starts up and connects to the database. Since the app hasn't been deployed/run against OCI yet, the tables weren't created.

---

## How to Test (3 Options)

### Option 1: Test Locally First (Recommended for Validation)

This validates the code works before deploying to OCI:

```bash
cd UserSubscription

# Build
mvn clean package -DskipTests

# Run with LOCAL MySQL (requires MySQL on localhost:3306)
java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=local
```

**Expected**: 
1. Application starts in ~10 seconds
2. Logs show: `Initializing database schema...`
3. Tables created automatically
4. API available at `http://localhost:8080/swagger-ui.html`

**To verify tables were created**:
```bash
# In another terminal, connect to local MySQL:
mysql -u root -p userdb
SELECT table_name FROM information_schema.tables WHERE table_schema='userdb';
```

**Expected output**:
```
+-----------------------+
| TABLE_NAME            |
+-----------------------+
| users                 |
| audit_log             |
+-----------------------+
```

---

### Option 2: Deploy to OCI Using Container Instances

This deploys the app directly to OCI (requires Docker):

**Step 1: Build Docker image**
```bash
cd UserSubscription

# Create Dockerfile
cat > Dockerfile << 'EOF'
FROM openjdk:21-slim
COPY target/user-subscription-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=oci"]
EOF

# Build and push to OCI Container Registry
mvn clean package -DskipTests
docker build -t user-subscription:1.0.0 .

# Tag for OCI Registry (replace REGION and TENANCY)
docker tag user-subscription:1.0.0 [REGION].ocir.io/[TENANCY]/user-subscription:1.0.0
docker push [REGION].ocir.io/[TENANCY]/user-subscription:1.0.0
```

**Step 2: Deploy using OCI Container Instances**
```bash
oci compute container-instances create-container-instance \
  --compartment-id "ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q" \
  --container-image-url "[REGION].ocir.io/[TENANCY]/user-subscription:1.0.0" \
  --region us-ashburn-1
```

---

### Option 3: Deploy Using OCI Bastion + SSH Tunnel (Advanced)

For MySQL compatible connection over TLS:

**Step 1: Create bastion host**
```bash
oci bastion create-bastion \
  --compartment-id "ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q" \
  --bastion-type "standard"
```

**Step 2: Create SSH tunnel**
```bash
ssh -L 3306:adb.us-ashburn-1.oraclecloud.com:3306 opc@bastion-host
```

**Step 3: Connect application through tunnel**
```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/usersubdb?useSSL=true"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="UserSubscription@123"
java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=oci
```

---

## Simplest Immediate Test (Local)

Since the code functionality is what matters, test locally first:

```bash
cd UserSubscription

# 1. Build
mvn clean package -DskipTests

# 2. Make sure MySQL is running
brew services list | grep mysql

# 3. Run with local MySQL (creates tables automatically)
java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=local

# Expected log output:
# 2026-03-07 12:31:46.123 INFO  Initializing database schema...
# 2026-03-07 12:31:46.456 INFO  Creating users table...
# 2026-03-07 12:31:46.789 INFO  Created audit_log table...
# 2026-03-07 12:31:47.000 INFO  Tomcat started on port(s): 8080
```

---

## Verify Tables Were Created

### After running application locally:

```bash
# Connect to MySQL
mysql -u root -p userdb

# Check tables
SHOW TABLES;

# Check users table structure
DESC users;

# Expected output:
# +-------+-----------+------+-----+---------+-------+
# | Field | Type      | Null | Key | Default | Extra |
# +-------+-----------+------+-----+---------+-------+
# | id    | varchar   | NO   | PRI |         |       |
# | name  | varchar   | NO   |     |         |       |
# | age   | int       | YES  |     |         |       |
# | city  | varchar   | YES  |     |         |       |
# | ...   | ...       | ...  | ... | ...     | ...   |
# +-------+-----------+------+-----+---------+-------+
```

---

## Test the API

Once application is running locally:

```bash
# 1. Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "age": 30,
    "city": "New York",
    "company": "Acme Corp",
    "interests": ["coding", "music"]
  }'

# Expected response:
# {
#   "id": "550e8400-e29b-41d4-a716-446655440000",
#   "name": "John Doe",
#   "age": 30,
#   "city": "New York",
#   "company": "Acme Corp",
#   "interests": ["coding", "music"],
#   "createdAt": "2026-03-07T12:31:47.000Z",
#   "updatedAt": "2026-03-07T12:31:47.000Z"
# }

# 2. Get all users
curl http://localhost:8080/api/v1/users

# 3. Verify data in database
mysql -u root -p userdb << EOF
SELECT id, name, age, city FROM users;
EOF
```

---

## Run Full Test Suite

```bash
# All tests (including E2E with TestContainers)
mvn test

# Just E2E tests
mvn test -Dtest=UserSubscriptionIntegrationE2ETest

# Expected output:
# [INFO] -------------------------------------------------------
# [INFO] T E S T S
# [INFO] -------------------------------------------------------
# [INFO] Running com.agentic.subscription.UserSubscriptionIntegrationE2ETest
# [INFO] Tests run: 9, Failures: 0, Errors: 0
# [INFO] -------------------------------------------------------
```

---

## Recommended Next Steps

**For Development/Testing:**
1. ✅ Test locally with MySQL (validates code works)
2. ✅ Verify tables are created and data persists
3. ✅ Run E2E test suite
4. ✅ Test API via Swagger UI

**For OCI Deployment:**
1. Build Docker image
2. Push to OCI Container Registry
3. Deploy to OCI Container Instances
4. Configure networking (security groups, etc.)
5. Verify tables created and data persists

---

## Common Issues & Fixes

**MySQL not running locally?**
```bash
brew install mysql@8.0
brew services start mysql@8.0
mysql -u root -p
# Password: UserSubscription@123
```

**Can't connect to OCI database?**
- Database needs: Bastion host OR VPN access OR MySQL tunnel
- For simplest testing: Use local MySQL first
- For production: Use OCI Container Instances with proper networking

**Tables not created?**
- Check logs: `grep -i "initializing\|creating" oci-app-test.log`
- Verify database connection: Check `application-oci.yml`
- Manually create: Run SQL from `DatabaseConfiguration.java`

---

## What the Code Does (Auto-Init)

When application starts with any database:

1. **Checks database exists** → Creates if missing (MySQL/PostgreSQL only)
2. **Checks schema exists** → Creates if missing (ALL databases)
3. **Executes DDL** → Creates `users` and `audit_log` tables
4. **Creates indexes** → For performance optimization
5. **Ready for API** → REST endpoints immediately available

---

## Summary

| Environment | Status | Next Action |
|-------------|--------|-------------|
| **Local MySQL** | ✅ Ready | `java -jar ... --spring.profiles.active=local` |
| **OCI Autonomous DB** | ⏳ Available | Deploy app to OCI or use bastion tunnel |
| **Code/Tests** | ✅ Ready | `mvn test` |
| **API** | ✅ Ready | `http://localhost:8080/swagger-ui.html` |

**Recommendation**: Test locally first to validate everything works, then deploy to OCI.
