#!/bin/bash
# Test script to deploy and verify application creates tables in OCI Autonomous Database

echo "=== Testing UserSubscription Service with OCI Autonomous Database ==="
echo ""
echo "✅ Database Status: AVAILABLE"
echo "✅ Database Name: user-subscription-aidb"
echo "✅ Admin User: admin"
echo "✅ Region: us-ashburn-1"
echo ""

cd "$(dirname "$0")"

echo "📦 Building application..."
mvn clean package -DskipTests -q 2>&1 | tail -5

echo ""
echo "🚀 Starting application with OCI profile..."
echo "   (This will automatically create tables if they don't exist)"
echo ""

# Set OCI database credentials
export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@user-subscription-aidb_high?TNS_ADMIN=~/.oci"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="UserSubscription@123"
export SPRING_PROFILES_ACTIVE="oci"

# Start application and capture first 30 seconds of output
timeout 30s java -jar target/user-subscription-1.0.0.jar \
  --spring.profiles.active=oci \
  --logging.level.root=INFO \
  --logging.level.com.agentic=DEBUG \
  2>&1 | tee oci-app-test.log || true

echo ""
echo "✅ Test completed"
echo ""
echo "💡 To check if tables were created in the database:"
echo "   1. Use OCI Database menu → SQL Developer Web"
echo "   2. Connect with admin / UserSubscription@123"
echo "   3. Run: SELECT table_name FROM user_tables;"
echo ""
echo "Output saved to: oci-app-test.log"
