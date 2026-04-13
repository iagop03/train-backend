terraform {
  required_version = ">= 1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
    google-beta = {
      source  = "hashicorp/google-beta"
      version = "~> 5.0"
    }
  }

  backend "gcs" {
    bucket = "train-project-terraform-state"
    prefix = "prod/cloudsql"
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

provider "google-beta" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

# VPC Network
resource "google_compute_network" "train_vpc" {
  name                    = "train-vpc"
  auto_create_subnetworks = false
  routing_mode            = "REGIONAL"

  depends_on = []
}

# Subnet para Cloud SQL (Private Service Connection)
resource "google_compute_subnetwork" "cloudsql_subnet" {
  name          = "cloudsql-subnet"
  ip_cidr_range = "10.1.0.0/24"
  region        = var.gcp_region
  network       = google_compute_network.train_vpc.id

  private_ip_google_access = true
  log_config {
    aggregation_interval = "INTERVAL_5_SEC"
    flow_sampling        = 0.5
    metadata             = "INCLUDE_ALL_METADATA"
  }
}

# Subnet para aplicaciones (GKE, App Engine, etc)
resource "google_compute_subnetwork" "app_subnet" {
  name          = "app-subnet"
  ip_cidr_range = "10.0.0.0/24"
  region        = var.gcp_region
  network       = google_compute_network.train_vpc.id

  private_ip_google_access = true
  log_config {
    aggregation_interval = "INTERVAL_5_SEC"
    flow_sampling        = 0.5
    metadata             = "INCLUDE_ALL_METADATA"
  }
}

# Private Service Connection para Cloud SQL
resource "google_compute_global_address" "private_ip_address" {
  name          = "train-cloudsql-private-ip"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.train_vpc.id
}

resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.train_vpc.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_address.name]
}

# Cloud SQL PostgreSQL Instance
resource "google_sql_database_instance" "train_postgres" {
  name                = "train-postgres-prod"
  database_version    = "POSTGRES_15"
  region              = var.gcp_region
  deletion_protection = true

  depends_on = [google_service_networking_connection.private_vpc_connection]

  settings {
    tier              = "db-custom-4-16384" # 4 vCPU, 16GB RAM
    availability_type = "REGIONAL"
    disk_size         = 100
    disk_type         = "PD_SSD"
    disk_autoresize   = true
    disk_autoresize_limit = 500

    # Backup Configuration
    backup_configuration {
      enabled                        = true
      start_time                     = "03:00"
      point_in_time_recovery_enabled = true
      transaction_log_retention_days = 7
      backup_retention_settings {
        retained_backups = 30
        retention_unit   = "COUNT"
      }
    }

    # IP Configuration - Private IP
    ip_configuration {
      ipv4_enabled                                  = false
      private_network                               = google_compute_network.train_vpc.id
      enable_private_path_for_cloudsql_cloud_shell = true
      require_ssl                                   = true

      # Authorized networks (if needed for dev)
      dynamic "authorized_networks" {
        for_each = var.authorized_networks
        content {
          name  = authorized_networks.value.name
          value = authorized_networks.value.cidr
        }
      }
    }

    # High Availability
    database_flags {
      name  = "cloudsql_iam_authentication"
      value = "on"
    }

    database_flags {
      name  = "log_statement"
      value = "all"
    }

    database_flags {
      name  = "log_min_duration_statement"
      value = "1000" # Log queries > 1s
    }

    database_flags {
      name  = "max_connections"
      value = "200"
    }

    # Maintenance Window
    maintenance_window {
      kind            = "MYSQL"
      day             = 3 # Wednesday
      hour            = 2
      update_track    = "stable"
    }

    # Insights Configuration
    insights_config {
      query_insights_enabled  = true
      query_plans_per_minute  = 5
      query_string_length     = 1024
      record_application_tags = true
    }
  }
}

# PostgreSQL Database
resource "google_sql_database" "train_db" {
  name     = "train"
  instance = google_sql_database_instance.train_postgres.name
  charset  = "UTF8"
  collation = null
}

# Root User
resource "google_sql_user" "root" {
  name     = "postgres"
  instance = google_sql_database_instance.train_postgres.name
  password = random_password.root_password.result
  type     = "BUILT_IN"
}

# Application User
resource "google_sql_user" "app_user" {
  name     = "train_app"
  instance = google_sql_database_instance.train_postgres.name
  password = random_password.app_password.result
  type     = "BUILT_IN"
}

# IAM Service Account User
resource "google_sql_user" "iam_user" {
  name     = google_service_account.cloudsql_client.email
  instance = google_sql_database_instance.train_postgres.name
  type     = "CLOUD_IAM_SERVICE_ACCOUNT"
}

# Service Account para conexión con Cloud SQL
resource "google_service_account" "cloudsql_client" {
  account_id   = "train-cloudsql-client"
  display_name = "TrAIn Cloud SQL Client"
}

resource "google_project_iam_member" "cloudsql_client_role" {
  project = var.gcp_project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.cloudsql_client.email}"
}

# SSL Certificate
resource "google_sql_ssl_cert" "client_cert" {
  common_name = "train-app-client"
  instance    = google_sql_database_instance.train_postgres.name
}

# CloudSQL Proxy Service Account (para GKE/App Engine)
resource "google_service_account" "cloudsql_proxy" {
  account_id   = "train-cloudsql-proxy"
  display_name = "TrAIn Cloud SQL Proxy"
}

resource "google_project_iam_member" "cloudsql_proxy_role" {
  project = var.gcp_project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.cloudsql_proxy.email}"
}

# Firewall - Allow internal traffic only
resource "google_compute_firewall" "cloudsql_internal" {
  name    = "allow-cloudsql-internal"
  network = google_compute_network.train_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["5432"]
  }

  source_ranges = [
    "10.0.0.0/24",    # app-subnet
    "10.1.0.0/24",    # cloudsql-subnet
  ]
}

# Random passwords
resource "random_password" "root_password" {
  length  = 32
  special = true
}

resource "random_password" "app_password" {
  length  = 32
  special = true
}

# Outputs
output "cloudsql_instance_connection_name" {
  value       = google_sql_database_instance.train_postgres.connection_name
  description = "Cloud SQL instance connection name"
}

output "cloudsql_private_ip" {
  value       = google_sql_database_instance.train_postgres.private_ip_address
  description = "Cloud SQL private IP address"
}

output "cloudsql_instance_self_link" {
  value       = google_sql_database_instance.train_postgres.self_link
  description = "Cloud SQL instance self link"
}

output "root_password" {
  value       = random_password.root_password.result
  sensitive   = true
  description = "Root password for Cloud SQL"
}

output "app_password" {
  value       = random_password.app_password.result
  sensitive   = true
  description = "Application user password"
}

output "ssl_client_cert" {
  value = google_sql_ssl_cert.client_cert.cert
  sensitive = true
  description = "Client SSL certificate"
}

output "ssl_client_key" {
  value = google_sql_ssl_cert.client_cert.private_key
  sensitive = true
  description = "Client SSL private key"
}

output "ssl_server_ca_cert" {
  value = google_sql_ssl_cert.client_cert.server_ca_cert
  sensitive = true
  description = "Server CA certificate"
}

output "cloudsql_client_service_account" {
  value = google_service_account.cloudsql_client.email
  description = "Cloud SQL client service account email"
}

output "cloudsql_proxy_service_account" {
  value = google_service_account.cloudsql_proxy.email
  description = "Cloud SQL proxy service account email"
}