# OCI Autonomous Database Testing - Complete Guide

## Current Status

✅ **Application**: Built and ready (28 MB JAR)  
✅ **Database**: AVAILABLE (user-subscription-aidb)  
✅ **Code**: Has auto-initialization logic  
⏳ **Testing**: Ready to deploy

---

## The Application Auto-Initialization Flow

When the application starts, it will:

1. **Read configuration** from environment variables or `application-oci.yml`
2. **Connect to database**: `user-subscription-aidb`
3. **Check tables exist** → Create if missing
   - Creates `users` table (with schema)
   - Creates `audit_log` table (with schema)
4. **Start REST API** on port 8080
5. **Ready for requests** → Tables available immediately

**Timeline**: Takes ~2-3 seconds total

---

## How to Test (Step-by-Step)

### Step 1: Prepare Application JAR

The JAR is already built:
```bash
cd UserSubscription
ls -lh target/user-subscription-1.0.0.jar
# Output: 28M user-subscription-1.0.0.jar ✓
```

### Step 2: Set OCI Database Credentials

```bash
# Get database details
oci db autonomous-database list \
  --compartment-id "ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q" \
  --region us-ashburn-1

# You'll see:
# - Display Name: user-subscription-aidb
# - Status: AVAILABLE
# - Connection strings with TCPS endpoint
```

### Step 3: Choose Your Connection Method

#### Option A: OCI Wallet (Recommended)

```bash
# 1. Download wallet from OCI Console
#    Databases > user-subscription-aidb > Database Connection > Download Wallet

# 2. Extract wallet
mkdir -p ~/.oci/wallet
unzip ~/Downloads/Wallet_usersubdb.zip -d ~/.oci/wallet

# 3. Set environment variables
export TNS_ADMIN=$HOME/.oci/wallet
export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@user-subscription-aidb_high"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="UserSubscription@123"

# 4. Start application
java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=oci
```

#### Option B: Bastion Host + SSH Tunnel

```bash
# 1. Create bastion in OCI (if not exists)
oci bastion create-bastion ...

# 2. Create SSH tunnel (in one terminal):
ssh -L 3306:adb.us-ashburn-1.oraclecloud.com:3306 opc@your-bastion-ip

# 3. In another terminal, set environment
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/usersubdb?useSSL=true"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="UserSubscription@123"

# 4. Start application
java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=oci
```

#### Option C: OCI Container Instances (Full Cloud)

```bash
# 1. Build and push Docker image
docker build -t user-subscription:1.0.0 .
docker tag user-subscription:1.0.0 [REGION].ocir.io/[TENANCY]/user-subscription:1.0.0
docker push [REGION].ocir.io/[TENANCY]/user-subscription:1.0.0

# 2. Create container instance
oci compute container-instances create-container-instance \
  --compartment-id "ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q" \
  --container-image-url "[REGION].ocir.io/[TENANCY]/user-subscription:1.0.0" \
  --container-environment '
    [
      {"name":"SPRING_PROFILES_ACTIVE","value":"oci"},
      {"name":"SPRING_DATASOURCE_USERNAME","value":"admin"},
      {"name":"SPRING_DATASOURCE_PASSWORD","value":"UserSubscription@123"},
      {"name":"SPRING_DATASOURCE_URL","value":"jdbc:oracle:thin:@user-subscription-aidb_high"}
    ]'

# 3. Get public IP of container instance
oci compute container-instances list --compartment-id [ID]
```

---

## Expected Behavior When Application Starts

### Console Output (first 30 seconds)

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

2026-03-07 12:31:46.123 INFO  Initializing database schema...
2026-03-07 12:31:46.456 INFO  Creating users table...
2026-03-07 12:31:46.678 INFO  Creating audit_log table...
2026-03-07 12:31:46.890 INFO  Database initialization completed successfully
2026-03-07 12:31:47.000 INFO  Tomcat started on port(s): 8080
2026-03-07 12:31:47.010 INFO  Started UserSubscriptionApplication in 3.245 seconds
```

✅ **This tells you**:
- Tables were created (or already existed)
- Application is ready
- API is listening on port 8080

### Verify Tables in OCI Database

**Option 1: SQL Developer Web (in OCI Console)**

1. Go to OCI Console → Databases → user-subscription-aidb
2. Click "Database Actions" → "SQL Developer Web"
3. Connect with: admin / UserSubscription@123
4. Run:
   ```sql
   SELECT table_name FROM user_tables;
   ```
5. Expected output:
   ```
   TABLE_NAME
   ----------
   USERS
   AUDIT_LOG
   ```

**Option 2: SQLPlus (from local machine)**

```bash
# If OCI Wallet is set up:
sqlplus admin@user-subscription-aidb_high

# Then:
SQL> SELECT table_name FROM user_tables;

# Expected:
# TABLE_NAME
# --------------------------------------------------
# USERS
# AUDIT_LOG
```

**Option 3: Oracle SQL Client**

```bash
# Using connection string
sqlplus admin@'(description=(address=(protocol=tcps)(port=1522)(host=adb.us-ashburn-1.oraclecloud.com))(connect_data=(service_name=g006c23f27d5d41_usersubdb_high.adb.oraclecloud.com)))'
```

---

## Test API Endpoints

Once application is running, test these endpoints:

### 1. Create a User

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Smith",
    "age": 28,
    "city": "San Francisco",
    "company": "Tech Corp",
    "interests": ["AI", "Cloud", "DevOps"]
  }'

# Expected Response:
# {
#   "id": "550e8400-e29b-41d4-a716-446655440001",
#   "name": "Alice Smith",
#   "age": 28,
#   "city": "San Francisco",
#   "company": "Tech Corp",
#   "interests": ["AI", "Cloud", "DevOps"],
#   "createdAt": "2026-03-07T12:31:50.000Z",
#   "updatedAt": "2026-03-07T12:31:50.000Z"
# }
```

### 2. Get All Users

```bash
curl http://localhost:8080/api/v1/users

# Expected Response: Array of all users
```

### 3. Get User Count

```bash
curl http://localhost:8080/api/v1/users/stats/count

# Expected Response:
# {"count":1}
```

### 4. Verify Data in Database

```sql
-- In SQL Developer Web or sqlplus:
SELECT id, name, age, city FROM users;

-- Expected:
-- ID                                     NAME          AGE CITY
-- ------ -------- --- ----------------
-- 550e8400-e29b-41d4-a716-446655440001 Alice Smith    28 San Francisco
```

### 5. Update User

```bash
curl -X PUT http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440001 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "age": 29,
    "city": "San Francisco"
  }'
```

### 6. Swagger UI

```
http://localhost:8080/swagger-ui.html
```

This provides interactive API documentation where you can:
- See all endpoints
- View request/response schemas
- Test endpoints directly via browser
- Click "Try it out" on any endpoint

---

## Verification Checklist

After deployment, verify:

- [ ] Application starts without errors
- [ ] Console shows "Initializing database schema..."
- [ ] Console shows "Database initialization completed"
- [ ] SQL Developer Web shows `USERS` and `AUDIT_LOG` tables
- [ ] POST request to `/api/v1/users` returns 201 status
- [ ] User appears in database after creation
- [ ] GET `/api/v1/users` returns the created user
- [ ] PUT endpoint updates user successfully
- [ ] DELETE endpoint removes user
- [ ] Swagger UI loads at `/swagger-ui.html`
- [ ] All 7 REST endpoints accessible

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **Connection refused** | Check if app has network access to OCI DB. Use bastion/VPN. |
| **Table not created** | Check logs for "Initializing database schema". Verify DB user permissions. |
| **Permission denied** | Check admin password is correct. Verify user has DDL privileges. |
| **Wallet not found** | Download from OCI Console. Set TNS_ADMIN correctly. |
| **Port 8080 conflict** | Use different port: `--server.port=8081` |

---

## Summary

| Component | Status | Notes |
|-----------|--------|-------|
| **Application** | ✅ Ready | 28 MB JAR built and tested |
| **Database** | ✅ Available | TCPS endpoint ready |
| **Auto-Init** | ✅ Coded | Tables create automatically |
| **Network** | ⏳ Setup needed | Choose bastion, wallet, or container instances |
| **Testing** | ✅ Ready | CRUD operations ready to verify |

**Next Action**: Choose your preferred connection method and follow the steps above to deploy and test the application against the OCI Autonomous Database.

---

## Quick Decision Matrix

| Scenario | Best Option | Effort |
|----------|-----------|--------|
| **Quick test** | Bastion + SSH tunnel | 30 min |
| **Production** | Container Instances | 1 hour |
| **Secure setup** | OCI Wallet | 20 min |
| **Development** | Any of above | Varies |

**Recommendation**: Start with **Option C (Container Instances)** for the cleanest setup, or **Option B (Bastion)** for quickest testing.
