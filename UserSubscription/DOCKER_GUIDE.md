# Docker Setup Guide for UserSubscription Application

This guide explains how to containerize and run your UserSubscription Spring Boot application with OCI Autonomous Database.

## Prerequisites

- Docker installed (Docker Desktop or Docker Engine)
- OCI Autonomous Database wallet downloaded and extracted
- Database credentials (username and password)
- JDBC URL from OCI Console

## Prerequisites for Building

Before building the Docker image, you need to have your OCI wallet extracted in the project:

```bash
# Create wallet directory in project root
mkdir -p UserSubscription/wallet

# Copy your extracted wallet files here
cp ~/.oci/wallet/* UserSubscription/wallet/
```

Your project structure should look like:
```
UserSubscription/
├── Dockerfile
├── wallet/
│   ├── cwallet.sso
│   ├── ewallet.p12
│   ├── tnsnames.ora
│   ├── sqlnet.ora
│   └── ojdbc.properties (optional)
├── pom.xml
├── src/
└── target/
```

## Building the Docker Image

### Build the image locally:

```bash
cd UserSubscription
docker build -t user-subscription:1.0.0 .
```

The Dockerfile will:
1. Build your Spring Boot JAR using Maven
2. Copy the JAR to the runtime container
3. **Copy your wallet directory into the image** (`/app/wallet`)
4. Set proper file permissions for wallet security

### Build with specific Java version tag:

```bash
docker build -t user-subscription:latest -t user-subscription:1.0.0 .
```

## Running the Container

The wallet is **already included in the image**, so you only need to provide database credentials:

```bash
docker run -d \
  --name user-subscription \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD="<your-database-password>" \
  user-subscription:1.0.0
```

That's it! The container has:
- ✅ Built JAR included
- ✅ Wallet files included (`/app/wallet`)
- ✅ All dependencies bundled
- ✅ Ready for production deployment

## Environment Variables

The container requires these environment variables:

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | No | `oci` | Spring profile (should be `oci` for Autonomous DB) |
| `SPRING_DATASOURCE_USERNAME` | Yes | `admin` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Yes | None | Database password |
| `SPRING_DATASOURCE_URL` | No | `jdbc:oracle:thin:@usersubdb_high?TNS_ADMIN=/app/wallet` | JDBC connection URL |
| `TNS_ADMIN` | No | `/app/wallet` | Path to wallet directory inside container |
| `DB_DRIVER` | No | `oracle.jdbc.OracleDriver` | JDBC driver class |

### Example with all variables:

```bash
docker run -d \
  --name user-subscription \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=oci \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD="YourPassword123" \
  -e SPRING_DATASOURCE_URL="jdbc:oracle:thin:@usersubdb_high?TNS_ADMIN=/app/wallet" \
  -e TNS_ADMIN=/app/wallet \
  -e DB_DRIVER=oracle.jdbc.OracleDriver \
  user-subscription:1.0.0
```

## Verifying the Container

### Check container status:

```bash
docker ps
# Look for user-subscription container
```

### View logs:

```bash
docker logs user-subscription
# Should show: "Application started in X seconds"
```

### Check health:

```bash
docker exec user-subscription curl http://localhost:8080/api/v1/users/health
# Should return: {"status":"UP"}
```

### Test API:

```bash
# Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","name":"Test User","address":"123 Main St"}'

# Get all users
curl http://localhost:8080/api/v1/users

# Access Swagger UI
curl http://localhost:8080/swagger-ui.html
```

## Wallet Setup

### Wallet Location in Project

Your wallet files must be copied to the project before building:

```bash
# Create wallet directory
mkdir -p UserSubscription/wallet

# Copy wallet files from OCI download
cp ~/Downloads/Wallet_usersubdb/cwallet.sso UserSubscription/wallet/
cp ~/Downloads/Wallet_usersubdb/ewallet.p12 UserSubscription/wallet/
cp ~/Downloads/Wallet_usersubdb/tnsnames.ora UserSubscription/wallet/
cp ~/Downloads/Wallet_usersubdb/sqlnet.ora UserSubscription/wallet/
cp ~/Downloads/Wallet_usersubdb/ojdbc.properties UserSubscription/wallet/  # optional
```

### Wallet Permissions Handled Automatically

The Dockerfile sets proper permissions during build:
- `cwallet.sso`: 600 (read/write owner only) ✓
- `ewallet.p12`: 600 (read/write owner only) ✓
- `tnsnames.ora`: 644 (readable by all) ✓
- `sqlnet.ora`: 644 (readable by all) ✓

These are set in the Dockerfile and don't need to be done manually.

## Dockerfile Explanation

### Stage 1: Builder
- Uses `maven:3.9-eclipse-temurin-21` to build the application
- Copies source code and builds JAR file
- Result: `user-subscription-1.0.0.jar`

### Stage 2: Runtime
- Uses lightweight `eclipse-temurin:21-jre-alpine` (minimal Java runtime)
- Copies only the built JAR from builder stage
- Copies wallet directory from build context into `/app/wallet`
- Sets environment variables
- Exposes port 8080
- Includes health check endpoint

## Docker Compose Setup (Optional)

Create a `docker-compose.yml` for easier management:

```yaml
version: '3.8'

services:
  user-subscription:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: user-subscription
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: oci
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_DATASOURCE_URL: jdbc:oracle:thin:@usersubdb_high?TNS_ADMIN=/app/wallet
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/v1/users/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

Then run:

```bash
export DB_PASSWORD="<your-database-password>"
docker-compose up -d
```

## Troubleshooting

### Issue: "ORA-28759: Failed to open wallet"

**Solution**: Ensure wallet files were copied into the image correctly:
```bash
docker exec user-subscription ls -la /app/wallet
# Should show: cwallet.sso, ewallet.p12, tnsnames.ora, etc.
```

### Issue: Connection timeout

**Solution**: Verify JDBC URL and TNS_ADMIN:
```bash
docker logs user-subscription | grep -i "datasource\|wallet\|tns"
```

### Issue: "Cannot find driver"

**Solution**: Ensure oracle JDBC driver is in classpath (it's already included in pom.xml). Rebuild image:
```bash
docker build --no-cache -t user-subscription:1.0.0 .
```

### View full startup logs:

```bash
docker logs -f user-subscription
```

## Removing the Container

```bash
# Stop the container
docker stop user-subscription

# Remove the container
docker rm user-subscription

# Remove the image
docker rmi user-subscription:1.0.0
```

## Production Considerations

1. **Use image registry**: Push image to Docker Hub, ECR, or ACR:
   ```bash
   docker tag user-subscription:1.0.0 myregistry/user-subscription:1.0.0
   docker push myregistry/user-subscription:1.0.0
   ```

2. **Environment variables**: Use `.env` files or secrets management:
   ```bash
   docker run --env-file .env.production user-subscription:1.0.0
   ```

3. **Resource limits**: Set CPU and memory limits:
   ```bash
   docker run -d --memory=512m --cpus=1 user-subscription:1.0.0
   ```

4. **Logging**: Configure container logging driver:
   ```bash
   docker run --log-driver splunk user-subscription:1.0.0
   ```

5. **Wallet security**: Never hardcode passwords in Dockerfile. Always use:
   - Environment variables (passed at runtime)
   - Docker secrets (for Swarm)
   - Kubernetes secrets (for K8s)

## Next Steps

- Deploy to Kubernetes (see [Kubernetes Deployment Guide](KUBERNETES_DEPLOYMENT.md))
- Deploy to OCI Container Instances
- Deploy to Docker Swarm
- Set up CI/CD pipeline for automatic image builds
