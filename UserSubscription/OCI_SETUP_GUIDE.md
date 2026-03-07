# OCI NoSQL Table Setup Guide

## Status
✅ Table creation initiated in OCI compartment `AgenticAI`  
📋 Work Request ID: `ocid1.nosqltableworkrequest.oc1.iad.amaaaaaalz6rytiaeftepir3yp3w47b5jdwpa6ufjmt6363pydmg3fhheyza`  

The table is being provisioned and should be available within 1-2 minutes.

## Two Ways to Set Up OCI NoSQL Table

### Option 1: Using OCI CLI (What We Started)

The table creation has been initiated with:

```bash
oci nosql table create \
  --compartment-id ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q \
  --name users \
  --ddl-statement "CREATE TABLE users(id STRING, name STRING, age INTEGER, city STRING, company STRING, interests ARRAY(STRING), createdAt LONG, updatedAt LONG, PRIMARY KEY(id))" \
  --table-limits '{"maxReadUnits": 10, "maxWriteUnits": 10, "maxStorageInGBs": 25}'
```

**Check Status:**
```bash
# Check if table is created
oci nosql table list --compartment-id ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q

# Get details of specific table
oci nosql table get --name users --compartment-id ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q
```

### Option 2: Using OCI Console (Manual Web Interface)

1. **Log in to OCI Console**
   - Go to https://www.oracle.com/cloud/sign-in/
   - Sign in with your OCI account

2. **Navigate to NoSQL Database**
   - From the hamburger menu, select **Databases** → **NoSQL Database Tables**
   - Make sure you're in the **AgenticAI** compartment

3. **Create Table**
   - Click **Create Table**
   - Fill in the details:
     - **Name:** `users`
     - **Compartment:** `AgenticAI`
     - **DDL Statement:**
       ```sql
       CREATE TABLE users(
         id STRING,
         name STRING,
         age INTEGER,
         city STRING,
         company STRING,
         interests ARRAY(STRING),
         createdAt LONG,
         updatedAt LONG,
         PRIMARY KEY(id)
       )
       ```
     - **Capacity Mode:** Fixed
     - **Read Capacity:** 10 RCU
     - **Write Capacity:** 10 WCU
     - **Storage:** 25 GB

4. **Wait for Table Creation**
   - Click Create
   - Table will be in "Creating" state for 1-2 minutes
   - Once "Active", it's ready to use

## Testing the Connection

Once the table is created and active, you can use OCI NoSQL with your Spring Boot service.

### Switch Your Service to OCI NoSQL

Edit `src/main/resources/application.yml`:

```yaml
app:
  persistence:
    type: oci-nosql  # Change from 'in-memory'
  oci:
    compartment-id: ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q
    region: us-ashburn-1
```

Then restart the service:

```bash
mvn spring-boot:run
```

### Test with Sample Data

Once service is running:

```bash
# Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "age": 28,
    "city": "San Francisco",
    "company": "CloudTech Inc",
    "interests": ["AI", "Cloud", "NoSQL"]
  }'

# List users from OCI NoSQL
curl -X GET http://localhost:8080/api/v1/users
```

## Troubleshooting

### Table Creation Hasn't Appeared Yet

Wait 1-2 more minutes and check:

```bash
oci nosql table list --compartment-id ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q
```

### Service Won't Connect to OCI

1. **Verify OCI Config**
   ```bash
   cat ~/.oci/config
   # Should have: user, fingerprint, key_file, tenancy, region
   ```

2. **Test OCI Access**
   ```bash
   oci iam compartment list
   ```

3. **Check Compartment ID**
   ```bash
   oci iam compartment get --compartment-id ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q
   ```

### Get More Details About Table Status

```bash
# Check work request status
oci nosql work-request list --compartment-id ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q

# Get specific work request
oci nosql work-request get --work-request-id ocid1.nosqltableworkrequest.oc1.iad.amaaaaaalz6rytiaeftepir3yp3w47b5jdwpa6ufjmt6363pydmg3fhheyza
```

## Compartment & Account Info

- **Compartment Name:** AgenticAI
- **Compartment ID:** `ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q`
- **Region:** us-ashburn-1
- **Table Name:** users
- **Desired Capacity:**
  - Read Units: 10 RCU
  - Write Units: 10 WCU
  - Storage: 25 GB

## Pricing Information

OCI NoSQL is billed based on:
- **Read requests** - 10 RCU allocated
- **Write requests** - 10 WCU allocated
- **Storage** - 25 GB limit

At these modest limits, costs should be minimal for testing/development.

## Next Steps

1. **Wait for table creation** - Usually takes 1-2 minutes
2. **Verify table is active** - Run list command above
3. **Update Spring Boot config** - Change `type: in-memory` to `type: oci-nosql`
4. **Restart service** - `mvn spring-boot:run`
5. **Test API** - Use curl examples provided

## Still In-Memory for Now

While we wait for OCI NoSQL table to be created, you can continue testing locally:

```bash
# Local testing (in-memory)
cd /Users/varunkashyap/AgenticCoding/UserSubscription
mvn spring-boot:run

# In another terminal, test API
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "age": 30,
    "city": "Austin",
    "company": "Test Corp",
    "interests": ["Testing"]
  }'
```

---

**Check back in 2 minutes for table creation completion!**
