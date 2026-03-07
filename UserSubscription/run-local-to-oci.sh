#!/bin/bash
# Direct Connection to OCI Autonomous Database from Local Machine
# No SSH tunnels, no containers - just direct connection

set -e

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  Setup Direct Connection to OCI Autonomous Database      ║"
echo "║  Run Application Locally, Write to OCI Database          ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Database Details
DB_NAME="user-subscription-aidb"
JDBC_URL="jdbc:oracle:thin:@adb.us-ashburn-1.oraclecloud.com:1522/g006c23f27d5d41_usersubdb_high.adb.oraclecloud.com"
DB_USER="admin"
DB_PASS="UserSubscription@123"

echo "📋 Database Configuration:"
echo "   Name: $DB_NAME"
echo "   Status: AVAILABLE"
echo "   JDBC URL: $JDBC_URL"
echo ""

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  STEP 1: Download Wallet from OCI Console                ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""
echo "This is required for certificate validation (TCPS protocol)"
echo ""
echo "Instructions:"
echo "  1. Go to: https://cloud.oracle.com"
echo "  2. Navigate: Databases → Autonomous Databases"
echo "  3. Select: user-subscription-aidb"
echo "  4. Click: Database Connection"
echo "  5. Click: Download Wallet"
echo "  6. Download as: Wallet_usersubdb.zip"
echo ""

read -p "Have you downloaded the wallet? (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Wallet is required for secure connection"
    echo "Please download from OCI Console and try again"
    exit 1
fi

echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  STEP 2: Extract Wallet                                  ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Check if wallet exists
WALLET_FILE="$HOME/Downloads/Wallet_usersubdb.zip"

if [ ! -f "$WALLET_FILE" ]; then
    echo "Looking for wallet in common locations..."
    # Try other locations
    WALLET_SEARCH=$(find "$HOME" -name "Wallet_usersubdb.zip" -o -name "Wallet_*.zip" 2>/dev/null | head -1)
    if [ -n "$WALLET_SEARCH" ]; then
        WALLET_FILE="$WALLET_SEARCH"
    else
        echo "❌ Wallet file not found"
        exit 1
    fi
fi

echo "✓ Found: $WALLET_FILE"
echo ""

WALLET_DIR="$HOME/.oci/wallet"
mkdir -p "$WALLET_DIR"

echo "Extracting wallet to: $WALLET_DIR"
unzip -o "$WALLET_FILE" -d "$WALLET_DIR" > /dev/null
echo "✓ Wallet extracted"
echo ""

# Verify wallet contents
echo "Wallet contents:"
ls -1 "$WALLET_DIR" | grep -E "\.pem|\.txt|\.ora"
echo ""

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  STEP 3: Run Application Locally                         ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

echo "Setting environment variables..."
export TNS_ADMIN="$WALLET_DIR"
export SPRING_DATASOURCE_URL="$JDBC_URL"
export SPRING_DATASOURCE_USERNAME="$DB_USER"
export SPRING_DATASOURCE_PASSWORD="$DB_PASS"
export DB_DRIVER="oracle.jdbc.OracleDriver"

echo "✓ TNS_ADMIN=$TNS_ADMIN"
echo "✓ SPRING_DATASOURCE_URL set"
echo "✓ SPRING_DATASOURCE_USERNAME=$DB_USER"
echo "✓ Ready to connect to OCI"
echo ""

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  STEP 4: Start Application                               ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

cd "$(dirname "$0")"

if [ ! -f "target/user-subscription-1.0.0.jar" ]; then
    echo "Building JAR..."
    mvn clean package -DskipTests -q
fi

echo "Starting application..."
echo "  Profile: oci"
echo "  Database: user-subscription-aidb (OCI)"
echo "  Connection: Direct TCPS (port 1522)"
echo ""
echo "The application will:"
echo "  1. Connect to OCI Autonomous Database"
echo "  2. Create tables automatically (if missing)"
echo "  3. Start REST API on port 8080"
echo ""
echo "Press Ctrl+C to stop"
echo ""

java -jar target/user-subscription-1.0.0.jar \
    --spring.profiles.active=oci \
    --logging.level.com.agentic=DEBUG \
    --logging.level.org.springframework.boot.autoconfigure=INFO

