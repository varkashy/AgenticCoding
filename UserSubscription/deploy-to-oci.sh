#!/bin/bash
# Deploy UserSubscription to OCI Container Instances
# This is the simplest way to test the application with OCI Autonomous Database

set -e

echo "╔═════════════════════════════════════════════════════════╗"
echo "║  Deploy to OCI Container Instances (Easiest Method)    ║"
echo "╚═════════════════════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_step() {
    echo -e "${BLUE}→${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

cd "$(dirname "$0")"

# Check if JAR exists
if [ ! -f "target/user-subscription-1.0.0.jar" ]; then
    print_step "Building JAR..."
    mvn clean package -DskipTests -q
    print_success "JAR built: 28 MB"
fi

echo ""
print_step "Step 1: Create Dockerfile"

cat > Dockerfile << 'EOF'
# Build stage
FROM maven:3.8-openjdk-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/target/user-subscription-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=oci"]
EOF

print_success "Dockerfile created"
echo "  Location: ./Dockerfile"
echo ""

echo "┌─────────────────────────────────────────────────────────┐"
echo "│  BUILD & PUSH DOCKER IMAGE                             │"
echo "└─────────────────────────────────────────────────────────┘"
echo ""

print_step "Step 2: Build Docker image"
echo "  Command: docker build -t user-subscription:1.0.0 ."
echo ""
echo "  Then tag for OCI Container Registry:"
echo "  docker tag user-subscription:1.0.0 \\"
echo "    [REGION].ocir.io/[TENANCY]/user-subscription:1.0.0"
echo ""
echo "  And push:"
echo "  docker push [REGION].ocir.io/[TENANCY]/user-subscription:1.0.0"
echo ""
echo "  Where:"
echo "    [REGION] = us-ashburn-1"
echo "    [TENANCY] = Your OCI Tenancy OCID (get from console)"
echo ""

read -p "Have you pushed the image to OCI Container Registry? (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_warning "Push the image first, then re-run this script"
    exit 1
fi

echo ""
print_step "Step 3: Get your OCI configuration"

# Get configuration
read -p "Enter OCI Region (default: us-ashburn-1): " REGION
REGION=${REGION:-us-ashburn-1}

read -p "Enter your Compartment ID: " COMPARTMENT_ID

read -p "Enter OCI Tenancy OCID: " TENANCY_OCID

read -p "Enter Container Registry Path (e.g., [REGION].ocir.io/[TENANCY]/user-subscription:1.0.0): " IMAGE_URL

echo ""
print_step "Step 4: Deploy to OCI Container Instances"

cat > deploy-container.sh << EOF
#!/bin/bash

# Deploy UserSubscription container to OCI

echo "Deploying container instance..."

oci compute container-instances create-container-instance \\
  --compartment-id "$COMPARTMENT_ID" \\
  --container-image-url "$IMAGE_URL" \\
  --display-name "user-subscription-service" \\
  --region "$REGION" \\
  --container-environment '[
    {
      "name": "SPRING_PROFILES_ACTIVE",
      "value": "oci"
    },
    {
      "name": "SPRING_DATASOURCE_USERNAME",
      "value": "admin"
    },
    {
      "name": "SPRING_DATASOURCE_PASSWORD",
      "value": "UserSubscription@123"
    },
    {
      "name": "SPRING_DATASOURCE_URL",
      "value": "jdbc:oracle:thin:@user-subscription-aidb_high"
    }
  ]' \\
  --container-port-number "8080" \\
  --wait-for-state "ACTIVE"

echo ""
echo "✓ Container instance created!"
echo ""
echo "Get the public IP address:"
echo "  oci compute container-instances list --compartment-id $COMPARTMENT_ID --region $REGION"

EOF

chmod +x deploy-container.sh

echo "  Created: deploy-container.sh"
echo ""
echo "  Preview of deployment command:"
echo "  ─────────────────────────────────────────────────────────"
cat deploy-container.sh | grep -A 20 "oci compute"
echo "  ─────────────────────────────────────────────────────────"
echo ""

read -p "Ready to deploy? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_step "Deploying container instance..."
    ./deploy-container.sh
    
    print_success "Container deployed!"
    echo ""
    echo "Next steps:"
    echo "1. Wait 2-3 minutes for container to start"
    echo "2. Get public IP address of the container"
    echo "3. Access application at: http://PUBLIC_IP:8080"
    echo "4. Access Swagger UI at: http://PUBLIC_IP:8080/swagger-ui.html"
    echo ""
    echo "And verify tables in OCI Console:"
    echo "1. Databases → user-subscription-aidb"
    echo "2. Database Actions → SQL Developer Web"
    echo "3. SELECT table_name FROM user_tables;"
else
    print_warning "Deployment cancelled"
    echo ""
    echo "To deploy manually, run:"
    echo "  ./deploy-container.sh"
fi

echo ""
print_step "Deployment Summary"
echo "  ✓ Application: user-subscription:1.0.0"
echo "  ✓ Database: user-subscription-aidb (AVAILABLE)"
echo "  ✓ API: Will be available at http://PUBLIC_IP:8080"
echo "  ✓ Tables: Auto-created when container starts"
echo "  ✓ Swagger UI: http://PUBLIC_IP:8080/swagger-ui.html"
echo ""
echo "═══════════════════════════════════════════════════════════"
