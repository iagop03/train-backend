#!/bin/bash
# MongoDB Atlas Monitoring Script
# Monitors cluster health, connections, and VPC peering status

set -e

PROJECT_ID=${1:-"train-project-id"}
CLUSTER_NAME="train-m0-cluster"

echo "=== MongoDB Atlas Monitoring Dashboard ==="
echo "Project: $PROJECT_ID"
echo "Cluster: $CLUSTER_NAME"
echo ""

# Function to check if MongoDB CLI tools are installed
check_tools() {
    if ! command -v mongosh &> /dev/null; then
        echo "❌ mongosh not found. Install with: brew install mongosh"
        return 1
    fi
    if ! command -v gcloud &> /dev/null; then
        echo "❌ gcloud CLI not found"
        return 1
    fi
    return 0
}

# Function to check VPC Peering Status
check_vpc_peering() {
    echo "📡 Checking VPC Peering Status..."
    gcloud compute networks peerings list --filter="name:train-mongodb-atlas-peering" --format="table(name,state)"
    echo ""
}

# Function to check MongoDB cluster status
check_cluster_status() {
    echo "🗄️  MongoDB Atlas Cluster Status..."
    # Requires Atlas CLI or API
    echo "  Use MongoDB Atlas Dashboard: https://cloud.mongodb.com/"
    echo "  Check: Clusters > train-m0-cluster > Status"
    echo ""
}

# Function to verify connectivity
verify_connectivity() {
    echo "🔗 Verifying MongoDB Atlas Connectivity..."
    
    MONGODB_URI=${MONGODB_URI:-"mongodb+srv://train_app_user:@train-m0-cluster.mongodb.net/train_db?retryWrites=true&w=majority"}
    
    if mongosh "$MONGODB_URI" --eval "db.adminCommand('ping')" 2>/dev/null; then
        echo "✅ MongoDB Atlas is reachable"
    else
        echo "❌ MongoDB Atlas is NOT reachable"
        echo "   Check: IP whitelist, VPC peering status, credentials"
    fi
    echo ""
}

# Function to check GCP Compute Engine instance connectivity
check_compute_engine() {
    echo "🖥️  GCP Compute Engine Connectivity..."
    
    # Get list of Compute Engine instances with MongoDB client
    INSTANCES=$(gcloud compute instances list --filter="name~'train-' AND status=RUNNING" --format="value(name)")
    
    if [ -z "$INSTANCES" ]; then
        echo "   No running Compute Engine instances found"
    else
        echo "   Testing connectivity from Compute Engine instances:"
        while IFS= read -r instance; do
            echo "   - Testing from: $instance"
            # This is a placeholder - actual SSH command would need SSH keys configured
            echo "     Run: gcloud compute ssh $instance -- 'nc -zv train-m0-cluster.mongodb.net 27017'"
        done <<< "$INSTANCES"
    fi
    echo ""
}

# Function to check Cloud SQL PostgreSQL
check_cloud_sql() {
    echo "🐘 GCP Cloud SQL PostgreSQL Status..."
    
    INSTANCES=$(gcloud sql instances list --format="table(name,state,databaseVersion)")
    if [ -z "$INSTANCES" ]; then
        echo "   No Cloud SQL instances found"
    else
        echo "$INSTANCES"
    fi
    echo ""
}

# Function to check firewall rules
check_firewall_rules() {
    echo "🔥 GCP Firewall Rules for MongoDB..."
    gcloud compute firewall-rules list --filter="name~'mongodb' OR targetTags:mongodb" --format="table(name,direction,sourceRanges,allowed)"
    echo ""
}

# Function to get network information
get_network_info() {
    echo "🌐 Network Information..."
    echo "GCP VPC Networks:"
    gcloud compute networks list --format="table(name,autoCreateSubnetworks,subnet_count=len(subnetworks.list()))"
    echo ""
}

# Function to monitor MongoDB connections
monitor_connections() {
    echo "📊 MongoDB Connection Monitoring (requires Atlas Dashboard)..."
    echo "   Open: https://cloud.mongodb.com/"
    echo "   Navigate: Clusters > train-m0-cluster > Metrics"
    echo "   Monitor: Connections, Network In/Out, Operations per second"
    echo ""
}

# Function to check recent errors in Cloud Logging
check_cloud_logs() {
    echo "📋 Recent GCP Logs (last 1 hour)..."
    gcloud logging read \
        "resource.type=gce_instance AND severity=ERROR" \
        --limit=10 \
        --format="table(timestamp,severity,jsonPayload.message)" \
        --freshness=1h 2>/dev/null || echo "   No errors found"
    echo ""
}

# Main execution
main() {
    echo "Checking tools..."
    if ! check_tools; then
        echo "❌ Required tools missing. Please install and try again."
        exit 1
    fi
    echo "✅ All tools available"
    echo ""
    
    check_vpc_peering
    check_cluster_status
    verify_connectivity
    check_compute_engine
    check_cloud_sql
    check_firewall_rules
    get_network_info
    monitor_connections
    check_cloud_logs
    
    echo "=== End of Monitoring Report ==="
}

main
