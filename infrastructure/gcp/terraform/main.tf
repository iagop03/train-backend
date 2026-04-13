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
    prefix = "gcp/state"
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

# Data source for current project
data "google_client_config" "current" {}
data "google_project" "project" {}

# VPC Network
resource "google_compute_network" "main" {
  name                    = var.vpc_network_name
  auto_create_subnetworks = false
  routing_mode            = "REGIONAL"
}

# Subnet
resource "google_compute_subnetwork" "main" {
  name          = "${var.vpc_network_name}-subnet"
  ip_cidr_range = var.subnet_cidr
  region        = var.gcp_region
  network       = google_compute_network.main.id

  private_ip_google_access = true

  log_config {
    aggregation_interval = "INTERVAL_5_SEC"
    flow_logs            = true
    metadata             = "INCLUDE_ALL_METADATA"
  }
}

# Private Service Connection for Cloud SQL
resource "google_compute_global_address" "private_ip_address" {
  name          = "private-ip-address"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.main.id
}

resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.main.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_address.name]
}

# Cloud SQL Instance (PostgreSQL)
resource "google_sql_database_instance" "postgres" {
  name             = var.cloud_sql_instance_name
  database_version = var.cloud_sql_database_version
  region           = var.gcp_region
  deletion_protection = true

  depends_on = [google_service_networking_connection.private_vpc_connection]

  settings {
    tier      = var.cloud_sql_tier
    disk_size = var.cloud_sql_disk_size
    disk_type = "PD_SSD"
    availability_type = "REGIONAL"
    backup_configuration {
      enabled                        = true
      start_time                     = "03:00"
      location                       = var.gcp_region
      point_in_time_recovery_enabled = true
      transaction_log_retention_days = 7
    }

    ip_configuration {
      require_ssl    = true
      private_network = google_compute_network.main.id
      ipv4_enabled   = false
    }

    database_flags {
      name  = "max_connections"
      value = "256"
    }

    database_flags {
      name  = "shared_buffers"
      value = "262144"
    }
  }
}

# Database
resource "google_sql_database" "train_db" {
  name     = var.database_name
  instance = google_sql_database_instance.postgres.name
  charset  = "UTF8"
}

# Database User
resource "google_sql_user" "app_user" {
  name     = var.database_user
  instance = google_sql_database_instance.postgres.name
  password = random_password.db_password.result
}

resource "google_sql_user" "postgres" {
  name     = "postgres"
  instance = google_sql_database_instance.postgres.name
  password = random_password.postgres_password.result
}

# Random passwords
resource "random_password" "db_password" {
  length  = 32
  special = true
}

resource "random_password" "postgres_password" {
  length  = 32
  special = true
}

# Cloud Run Service
resource "google_cloud_run_service" "backend" {
  name     = var.cloud_run_service_name
  location = var.gcp_region

  template {
    spec {
      service_account_name = google_service_account.cloud_run.email
      
      containers {
        image = "${var.container_registry}/${var.gcp_project_id}/${var.container_image_name}:${var.container_image_tag}"
        
        ports {
          container_port = 8080
        }
        
        resources {
          limits = {
            cpu    = var.cloud_run_cpu
            memory = var.cloud_run_memory
          }
        }
        
        env {
          name = "SPRING_DATASOURCE_URL"
          value = "jdbc:postgresql:///${var.database_name}?cloudSqlInstance=${google_sql_database_instance.postgres.connection_name}&socketFactory=com.google.cloud.sql.postgres.SocketFactory&user=${google_sql_user.app_user.name}&password=${random_password.db_password.result}"
        }
        
        env {
          name = "SPRING_JPA_HIBERNATE_DDL_AUTO"
          value = "validate"
        }
        
        env {
          name = "GCP_PROJECT_ID"
          value = var.gcp_project_id
        }
        
        env {
          name = "SPRING_CLOUD_GCP_PROJECT_ID"
          value = var.gcp_project_id
        }
      }
      
      timeout_seconds = var.cloud_run_timeout
    }
    
    metadata {
      annotations = {
        "run.googleapis.com/cloudsql-instances" = google_sql_database_instance.postgres.connection_name
        "run.googleapis.com/client-name"        = "terraform"
      }
    }
  }
  
  traffic {
    percent         = 100
    latest_revision = true
  }
  
  depends_on = [
    google_sql_user.app_user,
    google_cloud_run_service_iam_member.backend_public
  ]
}

# Allow unauthenticated access to Cloud Run
resource "google_cloud_run_service_iam_member" "backend_public" {
  service       = google_cloud_run_service.backend.name
  location      = google_cloud_run_service.backend.location
  role          = "roles/run.invoker"
  member        = "allUsers"
}

# Service Account for Cloud Run
resource "google_service_account" "cloud_run" {
  account_id   = "train-cloud-run"
  display_name = "Cloud Run Service Account for TrAIn"
}

# IAM Bindings
resource "google_project_iam_member" "cloud_run_cloudsql_client" {
  project = var.gcp_project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.cloud_run.email}"
}

resource "google_project_iam_member" "cloud_run_secret_accessor" {
  project = var.gcp_project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.cloud_run.email}"
}

resource "google_project_iam_member" "cloud_run_log_writer" {
  project = var.gcp_project_id
  role    = "roles/logging.logWriter"
  member  = "serviceAccount:${google_service_account.cloud_run.email}"
}

resource "google_project_iam_member" "cloud_run_metric_writer" {
  project = var.gcp_project_id
  role    = "roles/monitoring.metricWriter"
  member  = "serviceAccount:${google_service_account.cloud_run.email}"
}

# Cloud Build for CI/CD
resource "google_cloudbuild_trigger" "backend" {
  name     = "train-backend-trigger"
  filename = "cloudbuild.yaml"

  github {
    owner = var.github_owner
    name  = var.github_repo
    push {
      branch = var.github_branch
    }
  }

  service_account = google_service_account.cloud_build.id
}

# Service Account for Cloud Build
resource "google_service_account" "cloud_build" {
  account_id   = "train-cloud-build"
  display_name = "Cloud Build Service Account for TrAIn"
}

resource "google_project_iam_member" "cloud_build_run_admin" {
  project = var.gcp_project_id
  role    = "roles/run.admin"
  member  = "serviceAccount:${google_service_account.cloud_build.email}"
}

resource "google_project_iam_member" "cloud_build_iam_service_account_user" {
  project = var.gcp_project_id
  role    = "roles/iam.serviceAccountUser"
  member  = "serviceAccount:${google_service_account.cloud_build.email}"
}

resource "google_project_iam_member" "cloud_build_storage_admin" {
  project = var.gcp_project_id
  role    = "roles/storage.admin"
  member  = "serviceAccount:${google_service_account.cloud_build.email}"
}
