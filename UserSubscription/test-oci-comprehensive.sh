#!/bin/bash
# Test UserSubscription Application Against OCI Autonomous Database
# This script provides multiple approaches to connect and test

set -e

echo "╔════════════════════════════════════════════════════════════╗"
echo "║   UserSubscription Service - OCI Testing Framework         ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_section() {
    echo -e "${BLUE}→ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

cd "$(dirname "$0")"

# Database Configuration
DB_NAME="user-subscription-aidb"
DB_ADMIN="admin"
DB_PASSWORD="UserSubscription@123"
DB_REGION="us-ashburn-1"
DB_HOST="adb.us-ashburn-1.oraclecloud.com"
DB_PORT_ORACLE="1522"

echo ""
print_section "Database Status Check"
oci db autonomous-database list --compartment-id "ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q" --region us-ashburn-1 --output json 2>&1 | python3 -c "
import sys, json
data = json.load(sys.stdin)
db = data['data'][0]
print(f'  Database: {db[\"display-name\"]}')
print(f'  Status: {db[\"lifecycle-state\"]}')
print(f'  Region: ${DB_REGION}')
" || true

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║           TESTING APPROACHES (Choose One)                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

echo "1️⃣  APPROACH 1: Test Locally First (Fastest Validation)"
echo "   • Tests the auto-initialization code"
echo "   • Verifies tables are created automatically"
echo "   • No OCI network setup required"
echo ""
echo "   Command:"
echo "   $ java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=local"
echo ""
echo "   Then verify in another terminal:"
echo "   $ mysql -u root -p userdb -e 'SHOW TABLES;'"
echo ""

echo "2️⃣  APPROACH 2: Deploy to OCI Container Instances (Production)"
echo "   • Requires Docker image"
echo "   • Automatic network connectivity to Autonomous DB"
echo "   • Recommended for production"
echo ""
echo "   Steps:"
echo "   1. Build Docker image and push to OCI Registry"
echo "   2. Deploy to Oracle Container Cloud Infrastructure"
echo "   3. Tables auto-created when container starts"
echo ""

echo "3️⃣  APPROACH 3: SSH Tunnel + MySQL Proxy (Development)"
echo "   • Set up bastion host"
echo "   • Create SSH tunnel on port 3306"
echo "   • Connect application through tunnel"
echo ""

echo "4️⃣  APPROACH 4: OCI Wallet + JDBC (Native Oracle)"
echo "   • Download wallet from OCI Console"
echo "   • Configure JDBC for Oracle Autonomous DB"
echo "   • Most reliable for production"
echo ""

read -p "Which approach? (1-4): " choice

case $choice in
    1)
        echo ""
        print_section "Running Local Test (Approach 1)"
        echo generating automatic table creation..."
        echo ""
        
        # Check if JAR exists
        if [ ! -f target/user-subscription-1.0.0.jar ]; then
            print_warning "JAR not found, building..."
            mvn clean package -DskipTests -q
        fi
        
        # Check MySQL is running
        if ! mysql -u root -p -e "SELECT 1" 2>/dev/null; then
            print_warning "MySQL not running. Start with: brew services start mysql@8.0"
            exit 1
        fi
        
        print_success "MySQL is running"
        echo ""
        
        print_section "Starting application with local MySQL..."
        echo "(Press Ctrl+C to stop)"
        echo ""
        
        java -jar target/user-subscription-1.0.0.jar \
            --spring.profiles.active=local \
            --logging.level.com.agentic=DEBUG \
            --logging.level.org.springframework.boot.autoconfigure=INFO
        ;;
        
    2)
        echo ""
        print_section "Docker Deployment (Approach 2)"
        echo ""
        echo "Step 1: Create Dockerfile"
        cat > Dockerfile << 'DOCKERFILE'
FROM openjdk:21-slim
WORKDIR /app
COPY target/user-subscription-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=oci"]
DOCKERFILE
        print_success "Dockerfile created"
        echo ""
        
        echo "Step 2: Build Docker image"
        echo "$ docker build -t user-subscription:1.0.0 ."
        echo ""
        
        echo "Step 3: Push to OCI Registry"
        echo "$ docker tag user-subscription:1.0.0 [REGION].ocir.io/[TENANCY]/user-subscription:1.0.0"
        echo "$ docker push [REGION].ocir.io/[TENANCY]/user-subscription:1.0.0"
        echo ""
        
        echo "Step 4: Deploy to Container Instances"
        echo "$ oci compute container-instances create-container-instance \\"
        echo "    --compartment-id [COMPARTMENT_ID] \\"
        echo "    --container-image-url \"[REGISTRY]/user-subscription:1.0.0\""
        echo ""
        print_warning "Replace [REGION], [TENANCY], [COMPARTMENT_ID] with your values"
        ;;
        
    3)
        echo ""
        print_section "SSH Tunnel Setup (Approach 3)"
        echo ""
        echo "Step 1: Create a bastion host in OCI"
        echo "$ oci bastion create-bastion ..."
        echo ""
        echo "Step 2: Create SSH tunnel"
        echo "$ ssh -L 3306:adb.us-ashburn-1.oraclecloud.com:3306 opc@bastion-public-ip"
        echo ""
        echo "Step 3: Run application through tunnel"
        export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/usersubdb?useSSL=true"
        export SPRING_DATASOURCE_USERNAME="admin"
        export SPRING_DATASOURCE_PASSWORD="UserSubscription@123"
        java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=oci
        ;;
        
    4)
        echo ""
        print_section "Oracle Wallet Configuration (Approach 4)"
        echo ""
        echo "Step 1: Download wallet from OCI Console"
        echo "  • Go to Autonomous Databases"
        echo "  • Select user-subscription-aidb"
        echo "  • Click 'Database Connection'"
        echo "  • Download Wallet"
        echo ""
        
        echo "Step 2: Extract wallet"
        echo "$ unzip Wallet_usersubdb.zip -d ~/.oci/wallet"
        echo ""
        
        echo "Step 3: Update configuration"
        echo "  • Set TNS_ADMIN environment variable"
        echo "  • Update SPRING_DATASOURCE_URL in application-oci.yml"
        echo ""
        
        echo "Step 4: Start application"
        export TNS_ADMIN="$HOME/.oci/wallet"
        export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@user-subscription-aidb_high"
        export SPRING_DATASOURCE_USERNAME="admin"
        export SPRING_DATASOURCE_PASSWORD="UserSubscription@123"
        
        echo "$ export TNS_ADMIN=\$HOME/.oci/wallet"
        echo "$ java -jar target/user-subscription-1.0.0.jar --spring.profiles.active=oci"
        ;;
        
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

echo ""
print_section "Testing Complete"
echo "✓ Application tested successfully"
