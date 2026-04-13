#!/bin/bash

# Keycloak Deployment Script for GCP VM e2-small
# TRAIN-16: Deploy Keycloak with HTTPS and SSL Certificate

set -e

# Configuration Variables
KEYCLOAK_VERSION="23.0.0"
KEYCLOAK_ADMIN_USER="${KEYCLOAK_ADMIN_USER:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD}"
DOMAIN="${KEYCLOAK_DOMAIN}"
EMAIL="${LETSENCRYPT_EMAIL}"
INSTANCE_ZONE="${GCP_ZONE:-us-central1-a}"

echo "[INFO] Starting Keycloak deployment for TrAIn project..."

# Update system
echo "[INFO] Updating system packages..."
sudo apt-get update
sudo apt-get upgrade -y

# Install dependencies
echo "[INFO] Installing dependencies..."
sudo apt-get install -y \
    openjdk-21-jdk \
    wget \
    curl \
    git \
    nginx \
    certbot \
    python3-certbot-nginx \
    postgresql-client \
    ufw

# Configure firewall
echo "[INFO] Configuring firewall..."
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw --force enable

# Create keycloak user and directories
echo "[INFO] Creating Keycloak user and directories..."
sudo useradd -r -s /bin/bash keycloak || true
sudo mkdir -p /opt/keycloak
sudo mkdir -p /opt/keycloak/data
sudo mkdir -p /var/log/keycloak
sudo chown -R keycloak:keycloak /opt/keycloak /var/log/keycloak

# Download Keycloak
echo "[INFO] Downloading Keycloak ${KEYCLOAK_VERSION}..."
cd /tmp
wget https://github.com/keycloak/keycloak/releases/download/${KEYCLOAK_VERSION}/keycloak-${KEYCLOAK_VERSION}.tar.gz
sudo tar -xzf keycloak-${KEYCLOAK_VERSION}.tar.gz -C /opt/keycloak --strip-components=1
sudo chown -R keycloak:keycloak /opt/keycloak

# Configure Keycloak database connection
echo "[INFO] Configuring database connection..."
sudo tee /opt/keycloak/conf/keycloak.conf > /dev/null <<EOF
# Database Configuration
db=postgres
db-url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
db-username=${DB_USERNAME}
db-password=${DB_PASSWORD}

# Hostname Configuration
hostname=${DOMAIN}
hostname-admin=${DOMAIN}

# HTTP/HTTPS Configuration
http-enabled=false
https-certificate-file=/etc/letsencrypt/live/${DOMAIN}/fullchain.pem
https-certificate-key-file=/etc/letsencrypt/live/${DOMAIN}/privkey.pem
http-port=8080
https-port=8443

# Proxy Configuration
proxy=reencrypt
proxy-address-forwarding=on

# Admin Console
admin=true

# Metrics and Health
metrics-enabled=true
health-enabled=true

# Logging
log=file
log-file=/var/log/keycloak/keycloak.log
log-level=INFO
log-console-output=json
EOF

# Create systemd service file
echo "[INFO] Creating systemd service..."
sudo tee /etc/systemd/system/keycloak.service > /dev/null <<EOF
[Unit]
Description=Keycloak Identity Provider
After=syslog.target network-online.target remote-fs.target nss-lookup.target
Wants=network-online.target

[Service]
Type=simple
User=keycloak
Group=keycloak
ExecStart=/opt/keycloak/bin/kc.sh start
StandardOutput=journal
StandardError=journal
Restart=on-failure
RestartSec=5

Environment="JAVA_OPTS=-Xms256m -Xmx512m"
Environment="KEYCLOAK_ADMIN=${KEYCLOAK_ADMIN_USER}"
Environment="KEYCLOAK_ADMIN_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD}"

[Install]
WantedBy=multi-user.target
EOF

# Enable and start Keycloak service
echo "[INFO] Enabling Keycloak service..."
sudo systemctl daemon-reload
sudo systemctl enable keycloak.service

# Configure Nginx reverse proxy
echo "[INFO] Configuring Nginx reverse proxy..."
sudo tee /etc/nginx/sites-available/keycloak > /dev/null <<EOF
server {
    listen 80;
    server_name ${DOMAIN};
    return 301 https://\$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name ${DOMAIN};

    ssl_certificate /etc/letsencrypt/live/${DOMAIN}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${DOMAIN}/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    client_max_body_size 100M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;

        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Health check endpoint
    location /health {
        proxy_pass http://localhost:8080/health;
        access_log off;
    }
}
EOF

sudo ln -sf /etc/nginx/sites-available/keycloak /etc/nginx/sites-enabled/keycloak
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t

# Request SSL certificate with Let's Encrypt
echo "[INFO] Requesting SSL certificate from Let's Encrypt..."
sudo certbot certonly --standalone -d ${DOMAIN} \
    --email ${EMAIL} \
    --agree-tos \
    --non-interactive \
    --preferred-challenges http

# Create certificate renewal hook
echo "[INFO] Setting up certificate renewal hook..."
sudo mkdir -p /etc/letsencrypt/renewal-hooks/post
sudo tee /etc/letsencrypt/renewal-hooks/post/keycloak-restart.sh > /dev/null <<EOF
#!/bin/bash
echo "[INFO] Reloading Nginx after certificate renewal..."
nginx -s reload
echo "[INFO] Certificate renewal hook completed"
EOF

sudo chmod +x /etc/letsencrypt/renewal-hooks/post/keycloak-restart.sh

# Setup certificate auto-renewal with systemd timer
echo "[INFO] Setting up certificate auto-renewal..."
sudo tee /etc/systemd/system/certbot-renew.service > /dev/null <<EOF
[Unit]
Description=Let's Encrypt renewal
After=network-online.target
Wants=network-online.target

[Service]
Type=oneshot
ExecStart=/usr/bin/certbot renew --quiet --agree-tos
EOF

sudo tee /etc/systemd/system/certbot-renew.timer > /dev/null <<EOF
[Unit]
Description=Let's Encrypt renewal timer
Requires=certbot-renew.service

[Timer]
OnBootSec=10min
OnUnitActiveSec=1d
AccuracySec=60s

[Install]
WantedBy=timers.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable certbot-renew.timer
sudo systemctl start certbot-renew.timer

# Start Keycloak
echo "[INFO] Starting Keycloak service..."
sudo systemctl start keycloak.service

# Start Nginx
echo "[INFO] Starting Nginx..."
sudo systemctl restart nginx

# Wait for Keycloak to start
echo "[INFO] Waiting for Keycloak to start (this may take 2-3 minutes)..."
for i in {1..30}; do
    if curl -s -k https://localhost:8443/health | grep -q "UP"; then
        echo "[SUCCESS] Keycloak is running!"
        break
    fi
    echo "[INFO] Waiting... Attempt $i/30"
    sleep 10
done

echo ""
echo "================================================================"
echo "[SUCCESS] Keycloak deployment completed!"
echo "================================================================"
echo "Keycloak Admin Console: https://${DOMAIN}/admin"
echo "Keycloak Realm URL: https://${DOMAIN}/realms/train-gym"
echo "Default Admin User: ${KEYCLOAK_ADMIN_USER}"
echo ""
echo "[INFO] Logs can be viewed with: sudo journalctl -u keycloak -f"
echo "[INFO] Configuration file: /opt/keycloak/conf/keycloak.conf"
echo "================================================================"
