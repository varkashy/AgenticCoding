#!/bin/bash
# Quick Start Script for UserSubscription Service
# This script demonstrates the simplest way to get the application running

set -e

echo "======================================"
echo "UserSubscription Service Quick Start"
echo "======================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or later."
    exit 1
fi

echo "✅ Java version:"
java -version 2>&1 | head -1
echo ""

# Navigate to project directory
echo "📂 Building application..."
cd "$(dirname "$0")"
mvn clean package -DskipTests -q

JAR_FILE="target/user-subscription-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Build failed. JAR file not found at $JAR_FILE"
    exit 1
fi

JAR_SIZE=$(ls -lh "$JAR_FILE" | awk '{print $5}')
echo "✅ Build successful! JAR size: $JAR_SIZE"
echo ""

echo "======================================"
echo "Choose a deployment option:"
echo "======================================"
echo ""
echo "1) In-Memory Database (no setup required)"
echo "2) Local MySQL (requires MySQL on localhost:3306)"
echo "3) OCI Autonomous Database (requires connection string)"
echo ""

read -p "Enter your choice (1-3): " choice

case $choice in
    1)
        echo ""
        echo "🚀 Starting with in-memory database..."
        java -jar "$JAR_FILE" --app.persistence.type=in-memory
        ;;
    2)
        echo ""
        echo "🚀 Starting with local MySQL..."
        echo "   Make sure MySQL is running on localhost:3306"
        echo "   Database credentials: root / UserSubscription@123"
        echo ""
        java -jar "$JAR_FILE" --spring.profiles.active=local
        ;;
    3)
        echo ""
        echo "🚀 Starting with OCI Autonomous Database..."
        echo ""
        echo "You'll need:"
        echo "  - SPRING_DATASOURCE_URL (from OCI Console)"
        echo "  - SPRING_DATASOURCE_USERNAME (usually 'admin')"
        echo "  - SPRING_DATASOURCE_PASSWORD"
        echo ""
        
        read -p "Enter database URL (jdbc:mysql://...): " db_url
        read -p "Enter username: " db_user
        read -p "Enter password: " -s db_pass
        echo ""
        
        export SPRING_DATASOURCE_URL="$db_url"
        export SPRING_DATASOURCE_USERNAME="$db_user"
        export SPRING_DATASOURCE_PASSWORD="$db_pass"
        
        java -jar "$JAR_FILE" --spring.profiles.active=oci
        ;;
    *)
        echo "❌ Invalid choice"
        exit 1
        ;;
esac
