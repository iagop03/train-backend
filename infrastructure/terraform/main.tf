terraform {
  required_version = ">= 1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
  backend "gcs" {
    bucket = "train-terraform-state"
    prefix = "cloud-sql"
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

# VPC Network para Cloud SQL
resource "google_compute_network" "private_network" {
  name                    = "train-cloudsql-vpc"
  auto_create_subnetworks = false
  routing_mode            = "REGIONAL"

  depends_on = [google_project_service.compute_api]
}

# Subred privada para Cloud SQL
resource "google_compute_subnetwork" "private_subnet" {
  name          = "train-cloudsql-subnet"
  ip_cidr_range = "10.1.0.0/24"
  region        = var.gcp_region
  network       = google_compute_network.private_network.id
  private_ip_google_access = true

  log_config {
    aggregation_interval = "INTERVAL_5_SEC"
    flow_logs_enabled    = true
    metadata             = "INCLUDE_ALL_METADATA"
  }
}

# Private Service Connection para Cloud SQL
resource "google_compute_global_address" "private_ip_address" {
  name          = "train-cloudsql-private-ip"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.private_network.id
}

resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.private_network.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_address.name]
}

# Cloud SQL PostgreSQL Instance
resource "google_sql_database_instance" "train_postgres" {
  name                = "train-postgres-prod"
  database_version    = "POSTGRES_15"
  region              = var.gcp_region
  deletion_protection = true
  depends_on          = [google_service_networking_connection.private_vpc_connection]

  settings {
    tier              = "db-custom-2-8192"
    availability_type = "REGIONAL"
    disk_type         = "PD_SSD"
    disk_size         = 100
    disk_autoresize   = true
    disk_autoresize_limit = 500

    # Backup automatico diario
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

    # IP Privada - VPC
    ip_configuration {
      ipv4_enabled    = false
      private_network = google_compute_network.private_network.id
      require_ssl     = true
      
      authorized_networks {
        name  = "office"
        value = var.office_ip_cidr
      }
    }

    # Maintenance Window
    maintenance_window {
      day          = 3  # Wednesday
      hour         = 2  # 02:00 UTC
      update_track = "stable"
    }

    # Database Flags
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
      value = "1000"
    }
    database_flags {
      name  = "log_connections"
      value = "on"
    }
    database_flags {
      name  = "log_disconnections"
      value = "on"
    }
    database_flags {
      name  = "max_connections"
      value = "200"
    }

    insights_config {
      query_insights_enabled  = true
      query_string_length     = 1024
      record_application_tags = true
    }
  }

  deletion_protection = true
}

# SSL Certificate
resource "google_sql_ssl_cert" "train_ssl_cert" {
  common_name = "train-app-cert"
  instance    = google_sql_database_instance.train_postgres.name
}

# Root user con password seguro
resource "random_password" "db_root_password" {
  length  = 32
  special = true
}

resource "google_sql_user" "db_root" {
  name     = "postgres"
  instance = google_sql_database_instance.train_postgres.name
  password = random_password.db_root_password.result
}

# Usuario de aplicación con IAM
resource "google_sql_database_instance_iam_binding" "train_app_iam" {
  instance = google_sql_database_instance.train_postgres.name
  role     = "roles/cloudsql.client"

  members = [
    "serviceAccount:${google_service_account.train_app.email}",
  ]
}

# Service Account para la aplicación
resource "google_service_account" "train_app" {
  account_id   = "train-app-sa"
  display_name = "TrAIn Application Service Account"
}

resource "google_service_account_key" "train_app_key" {
  service_account_id = google_service_account.train_app.name
  public_key_type    = "TYPE_X509_PEM_FILE"
}

# Usuario de aplicación IAM
resource "google_sql_user" "train_app_user" {
  name     = "train_app"
  instance = google_sql_database_instance.train_postgres.name
  type     = "CLOUD_IAM_SERVICE_ACCOUNT"
}

# Base de datos principal
resource "google_sql_database" "train_db" {
  name     = "train_db"
  instance = google_sql_database_instance.train_postgres.name
  charset  = "UTF8"
}

# Habilitar Cloud SQL Admin API
resource "google_project_service" "cloudsql_api" {
  service            = "sqladmin.googleapis.com"
  disable_on_destroy = false
}

# Habilitar Compute API
resource "google_project_service" "compute_api" {
  service            = "compute.googleapis.com"
  disable_on_destroy = false
}

# Habilitar Service Networking API
resource "google_project_service" "servicenetworking_api" {
  service            = "servicenetworking.googleapis.com"
  disable_on_destroy = false
}

# Cloud Storage para backups adicionales
resource "google_storage_bucket" "backups" {
  name          = "train-cloudsql-backups-${var.gcp_project_id}"
  location      = var.gcp_region
  force_destroy = false

  versioning {
    enabled = true
  }

  lifecycle_rule {
    action {
      type          = "DeleteObject"
      storage_class = "ARCHIVE"
    }
    condition {
      age = 90
    }
  }

  encryption {
    default_kms_key_name = google_kms_crypto_key.backup_key.id
  }
}

# KMS Key para encriptación de backups
resource "google_kms_key_ring" "backup_keyring" {
  name     = "train-backup-keyring"
  location = var.gcp_region
}

resource "google_kms_crypto_key" "backup_key" {
  name            = "train-backup-key"
  key_ring        = google_kms_key_ring.backup_keyring.id
  rotation_period = "7776000s"  # 90 days
  version_template {
    algorithm = "GOOGLE_SYMMETRIC_ENCRYPTION"
  }
}

# Cloud SQL Proxy service account
resource "google_service_account" "cloudsql_proxy" {
  account_id   = "train-cloudsql-proxy"
  display_name = "Cloud SQL Proxy Service Account"
}

resource "google_project_iam_member" "cloudsql_proxy_role" {
  project = var.gcp_project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.cloudsql_proxy.email}"
}
