# Multi-stage Dockerfile for UserSubscription Spring Boot Application
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application (skip tests for faster build, enable for production)
RUN mvn clean package -DskipTests

# Stage 2: Runtime environment
FROM eclipse-temurin:21-jre-alpine

# Install required tools
RUN apk add --no-cache bash curl

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/user-subscription-1.0.0.jar app.jar

# Copy Oracle Wallet from build context
# Wallet files should be in ./wallet directory in project root
COPY wallet /app/wallet

# Set proper permissions for wallet files
RUN chmod 600 /app/wallet/cwallet.sso /app/wallet/ewallet.p12 && \
    chmod 644 /app/wallet/tnsnames.ora /app/wallet/sqlnet.ora && \
    chmod 644 /app/wallet/ojdbc.properties 2>/dev/null || true

# Environment variables for OCI Autonomous Database connection
# These MUST be provided at runtime via: docker run -e VAR_NAME=value
ENV SPRING_PROFILES_ACTIVE=oci
ENV TNS_ADMIN=/app/wallet
ENV SPRING_DATASOURCE_URL=jdbc:oracle:thin:@usersubdb_high?TNS_ADMIN=/app/wallet
ENV DB_DRIVER=oracle.jdbc.OracleDriver
ENV SPRING_DATASOURCE_USERNAME=admin

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/users/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
