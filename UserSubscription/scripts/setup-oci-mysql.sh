#!/bin/bash

################################################################################
# OCI MySQL Database System Setup Script
# This script creates a MySQL Database System in your OCI compartment
################################################################################

set -e

# Configuration
COMPARTMENT_ID="ocid1.compartment.oc1..aaaaaaaa5jyg67wth3xrzqb2xj6cwvc2urt44ykswetzqalfri4soyvxdg5q"
REGION="us-ashburn-1"
DB_DISPLAY_NAME="user-subscription-db"
DB_ADMIN_USER="root"
DB_ADMIN_PASSWORD="UserSubscription@123"
DB_MYSQL_VERSION="8.0.32"
DB_SHAPE="MySQL.VM.Standard.E4.1.32GB"

echo "======================================================================"
echo "OCI MySQL Database System Setup"
echo "======================================================================"
echo ""
echo "Configuration:"
echo "  Compartment ID: $COMPARTMENT_ID"
echo "  Region: $REGION"
echo "  Database Name: $DB_DISPLAY_NAME"
echo "  MySQL Version: $DB_MYSQL_VERSION"
echo "  Shape: $DB_SHAPE"
echo ""

# Step 1: Check OCI CLI
echo "[Step 1] Checking OCI CLI..."
if ! command -v oci &> /dev/null; then
    echo "ERROR: OCI CLI not found. Please install it first:"
    echo "  https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm"
    exit 1
fi
echo "✓ OCI CLI is installed ($(oci --version))"
echo ""

# Step 2: Get available VCNs
echo "[Step 2] Fetching available VCNs..."
VCN_LIST=$(oci network vcn list --compartment-id "$COMPARTMENT_ID" --region "$REGION" -o json 2>/dev/null || echo "{\"data\":[]}")

# Check if we have any VCNs
VCN_COUNT=$(echo "$VCN_LIST" | python3 -c "import sys, json; print(len(json.load(sys.stdin).get('data', [])))" 2>/dev/null || echo "0")

if [ "$VCN_COUNT" -eq 0 ]; then
    echo "⚠ No VCNs found in compartment. Creating a VCN..."
    
    # Create VCN
    VCN_RESPONSE=$(oci network vcn create \
        --compartment-id "$COMPARTMENT_ID" \
        --cidr-block "10.0.0.0/16" \
        --display-name "user-subscription-vcn" \
        --region "$REGION" \
        -o json)
    
    VCN_ID=$(echo "$VCN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")
    echo "✓ Created VCN: $VCN_ID"
else
    VCN_ID=$(echo "$VCN_LIST" | python3 -c "import sys, json; print(json.load(sys.stdin)['data'][0]['id'])")
    echo "✓ Found VCN: $VCN_ID"
fi
echo ""

# Step 3: Get available Subnets
echo "[Step 3] Fetching subnets for VCN..."
SUBNET_LIST=$(oci network subnet list --compartment-id "$COMPARTMENT_ID" --vcn-id "$VCN_ID" --region "$REGION" -o json 2>/dev/null || echo "{\"data\":[]}")

SUBNET_COUNT=$(echo "$SUBNET_LIST" | python3 -c "import sys, json; print(len(json.load(sys.stdin).get('data', [])))" 2>/dev/null || echo "0")

if [ "$SUBNET_COUNT" -eq 0 ]; then
    echo "⚠ No subnets found. Creating a subnet..."
    
    # Create subnet
    SUBNET_RESPONSE=$(oci network subnet create \
        --compartment-id "$COMPARTMENT_ID" \
        --vcn-id "$VCN_ID" \
        --cidr-block "10.0.1.0/24" \
        --display-name "user-subscription-subnet" \
        --region "$REGION" \
        -o json)
    
    SUBNET_ID=$(echo "$SUBNET_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")
    echo "✓ Created Subnet: $SUBNET_ID"
else
    SUBNET_ID=$(echo "$SUBNET_LIST" | python3 -c "import sys, json; print(json.load(sys.stdin)['data'][0]['id'])")
    echo "✓ Found Subnet: $SUBNET_ID"
fi
echo ""

# Step 4: Get Availability Domain
echo "[Step 4] Getting Availability Domain..."
AD=$(oci iam availability-domain list --region "$REGION" -o json 2>/dev/null | \
     python3 -c "import sys, json; print(json.load(sys.stdin)['data'][0]['name'])" 2>/dev/null)

if [ -z "$AD" ]; then
    AD="AD-1"  # Default fallback
fi
echo "✓ Using Availability Domain: $AD"
echo ""

# Step 5: Create MySQL Database System
echo "[Step 5] Creating MySQL Database System..."
echo "This may take 10-15 minutes..."
echo ""

CREATE_RESPONSE=$(oci mysql db-system create \
    --compartment-id "$COMPARTMENT_ID" \
    --subnet-id "$SUBNET_ID" \
    --display-name "$DB_DISPLAY_NAME" \
    --admin-username "$DB_ADMIN_USER" \
    --admin-password "$DB_ADMIN_PASSWORD" \
    --availability-domain "$AD" \
    --shape-name "$DB_SHAPE" \
    --mysql-version "$DB_MYSQL_VERSION" \
    --region "$REGION" \
    -o json)

DB_SYSTEM_ID=$(echo "$CREATE_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])" 2>/dev/null)

if [ -z "$DB_SYSTEM_ID" ]; then
    echo "ERROR: Failed to create MySQL Database System"
    echo "$CREATE_RESPONSE"
    exit 1
fi

echo "✓ MySQL Database System created: $DB_SYSTEM_ID"
echo ""
echo "Waiting for instance to be ACTIVE (this takes ~10-15 minutes)..."
echo ""

# Step 6: Wait for MySQL to be ready
oci mysql db-system get --db-system-id "$DB_SYSTEM_ID" --region "$REGION" \
    --wait-for-state "ACTIVE" \
    --max-wait-seconds 1800 \
    -o json > /dev/null 2>&1 || true

# Get final details
DB_DETAILS=$(oci mysql db-system get --db-system-id "$DB_SYSTEM_ID" --region "$REGION" -o json)
DB_HOSTNAME=$(echo "$DB_DETAILS" | python3 -c "import sys, json; print(json.load(sys.stdin)['data'].get('mysql_version', 'N/A'))" 2>/dev/null)
DB_STATE=$(echo "$DB_DETAILS" | python3 -c "import sys, json; print(json.load(sys.stdin)['data'].get('lifecycle_state', 'UNKNOWN'))" 2>/dev/null)

echo ""
echo "======================================================================"
echo "✓ MySQL Database System Setup Complete!"
echo "======================================================================"
echo ""
echo "Database Details:"
echo "  ID: $DB_SYSTEM_ID"
echo "  Name: $DB_DISPLAY_NAME"
echo "  State: $DB_STATE"
echo "  Admin User: $DB_ADMIN_USER"
echo ""
echo "Next Steps:"
echo "1. Wait for the instance to fully initialize (check OCI Console)"
echo "2. Get the database hostname from OCI Console:"
echo "   - Navigate to MySQL Database Service"
echo "   - Click on '$DB_DISPLAY_NAME'"
echo "   - Find 'Endpoints' section to get the hostname"
echo "3. Update application-oci.yml with the hostname:"
echo "   spring.datasource.url=jdbc:mysql://<hostname>:3306/userdb?useSSL=true"
echo "4. Deploy the application:"
echo "   java -jar user-subscription-1.0.0.jar --spring.profiles.active=oci"
echo ""
echo "Security Note:"
echo "  - Save the admin password securely: $DB_ADMIN_PASSWORD"
echo "  - Allow port 3306 from your app's security group"
echo "  - Consider creating a limited-privilege user for the app"
echo ""
