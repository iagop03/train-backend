#!/bin/bash

# Keycloak Startup Script for GCP VM
# Executed as startup script metadata

set -e

echo "[INFO] Starting Keycloak deployment..."

# Log output
exec > >(tee /var/log/keycloak-startup.log)
exec 2>&1

# Export variables
export KEYCLOAK_ADMIN="${keycloak_admin_user}"
export KEYCLOAK_ADMIN_PASSWORD="${keycloak_admin_password}"
export DB_HOST="${db_host}"
export DB_PORT="${db_port}"
export DB_NAME="${db_name}"
export DB_USERNAME="${db_username}"
export DB_PASSWORD="${db_password}"
export KEYCLOAK_DOMAIN="${keycloak_domain}"
export LETSENCRYPT_EMAIL="${letsencrypt_email}"
export GCP_ZONE="${gcp_region}-${gcp_zone}"

# Run deployment script
cat > /tmp/deploy-keycloak.sh <<'DEPLOY_SCRIPT'
#!/bin/bash
set -e

echo "[INFO] Updating system..."
sudo apt-get update
sudo apt-get upgrade -y

echo "[INFO] Installing Java 21..."
sudo apt-get install -y openjdk-21-jdk
java -version

echo "[INFO] Installing additional dependencies..."
sudo apt-get install -y \
    wget \
    curl \
    git \
    nginx \
    certbot \
    python3-certbot-nginx \
    postgresql-client \
    ufw \
    jq

echo "[INFO] Configuring firewall..."
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable

echo "[INFO] Creating keycloak user..."
sudo useradd -r -s /bin/bash keycloak || true

echo "[INFO] Creating Keycloak directories..."
sudo mkdir -p /opt/keycloak
sudo mkdir -p /var/log/keycloak
sudo chown -R keycloak:keycloak /opt/keycloak /var/log/keycloak

echo "[INFO] Downloading and extracting Keycloak 23.0.0..."
cd /tmp
wget -q https://github.com/keycloak/keycloak/releases/download/23.0.0/keycloak-23.0.0.tar.gz
sudo tar -xzf keycloak-23.0.0.tar.gz -C /opt/keycloak --strip-components=1
sudo chown -R keycloak:keycloak /opt/keycloak

echo "[INFO] Configuring Keycloak..."
sudo tee /opt/keycloak/conf/keycloak.conf > /dev/null <<'EOF'
db=postgres
db-url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
db-username=${DB_USERNAME}
db-password=${DB_PASSWORD}

hostname=${KEYCLOAK_DOMAIN}
hostname-admin=${KEYCLOAK_DOMAIN}

http-enabled=false
http-port=8080
https-port=8443

proxy=reencrypt
proxy-address-forwarding=on

admin=true

metrics-enabled=true
health-enabled=true

log=file
log-file=/var/log/keycloak/keycloak.log
log-level=INFO
log-console-output=json
EOF

echo "[INFO] Creating systemd service..."
sudo tee /etc/systemd/system/keycloak.service > /dev/null <<'EOF'
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
TimeoutStartSec=600

Environment="JAVA_OPTS=-Xms256m -Xmx512m"
Environment="KEYCLOAK_ADMIN=${KEYCLOAK_ADMIN}"
Environment="KEYCLOAK_ADMIN_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD}"

[Install]
WantedBy=multi-user.target
EOF

echo "[INFO] Configuring Nginx reverse proxy..."
sudo tee /etc/nginx/sites-available/keycloak > /dev/null <<'EOF'
server {
    listen 80;
    server_name ${KEYCLOAK_DOMAIN};
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name ${KEYCLOAK_DOMAIN};

    client_max_body_size 100M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;

        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /health {
        proxy_pass http://localhost:8080/health;
        access_log off;
    }
}
EOF

sudo ln -sf /etc/nginx/sites-available/keycloak /etc/nginx/sites-enabled/keycloak
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t

echo "[INFO] Requesting Let's Encrypt SSL certificate..."
sudo certbot certonly --standalone -d ${KEYCLOAK_DOMAIN} \
    --email ${LETSENCRYPT_EMAIL} \
    --agree-tos \
    --non-interactive \
    --preferred-challenges http || true

echo "[INFO] Creating certificate renewal hook..."
sudo mkdir -p /etc/letsencrypt/renewal-hooks/post
sudo tee /etc/letsencrypt/renewal-hooks/post/keycloak-restart.sh > /dev/null <<'HOOK'
#!/bin/bash
echo "[INFO] Reloading Nginx after certificate renewal..."
nginx -s reload
systemctl reload keycloak || true
HOOK

sudo chmod +x /etc/letsencrypt/renewal-hooks/post/keycloak-restart.sh

echo "[INFO] Updating Nginx configuration with SSL..."
sudo tee /etc/nginx/sites-available/keycloak > /dev/null <<'EOF'
server {
    listen 80;
    server_name ${KEYCLOAK_DOMAIN};
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name ${KEYCLOAK_DOMAIN};

    ssl_certificate /etc/letsencrypt/live/${KEYCLOAK_DOMAIN}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${KEYCLOAK_DOMAIN}/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    client_max_body_size 100M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;

        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /health {
        proxy_pass http://localhost:8080/health;
        access_log off;
    }
}
EOF

echo "[INFO] Setting up certificate auto-renewal..."
sudo systemctl daemon-reload
sudo systemctl enable certbot-renew.timer || true
sudo systemctl start certbot-renew.timer || true

echo "[INFO] Enabling Keycloak service..."
sudo systemctl daemon-reload
sudo systemctl enable keycloak.service
sudo systemctl start keycloak.service

echo "[INFO] Restarting Nginx..."
sudo systemctl restart nginx

echo "[INFO] Waiting for Keycloak to start..."
for i in {1..30}; do
    if curl -s -k https://localhost/health 2>/dev/null | grep -q '"status"' || curl -s -k http://localhost:8080/health 2>/dev/null | grep -q '"status"'; then
        echo "[SUCCESS] Keycloak is ready!"
        break
    fi
    echo "[INFO] Waiting... ($i/30)"
    sleep 10
done

echo ""
echo "========================================"
echo "[SUCCESS] Keycloak Deployment Complete!"
echo "========================================"
echo "Admin Console: https://${KEYCLOAK_DOMAIN}/admin"
echo "Realm URL: https://${KEYCLOAK_DOMAIN}/realms/train-gym"
echo ""
echo "Logs: sudo journalctl -u keycloak -f"
echo "========================================"
DEPLOY_SCRIPT

chmod +x /tmp/deploy-keycloak.sh
/tmp/deploy-keycloak.sh

echo "[INFO] Keycloak startup script completed!"
