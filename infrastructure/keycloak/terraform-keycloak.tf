# Terraform configuration for Keycloak deployment on GCP
# TRAIN-16: GCP VM e2-small for Keycloak with HTTPS

terraform {
  required_version = ">= 1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

# Network and firewall rules
resource "google_compute_firewall" "keycloak_http" {
  name    = "keycloak-allow-http"
  network = "default"

  allow {
    protocol = "tcp"
    ports    = ["80"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["keycloak"]
}

resource "google_compute_firewall" "keycloak_https" {
  name    = "keycloak-allow-https"
  network = "default"

  allow {
    protocol = "tcp"
    ports    = ["443"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["keycloak"]
}

resource "google_compute_firewall" "keycloak_ssh" {
  name    = "keycloak-allow-ssh"
  network = "default"

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = var.allowed_ssh_cidrs
  target_tags   = ["keycloak"]
}

# Static IP address
resource "google_compute_address" "keycloak_static_ip" {
  name   = "keycloak-static-ip"
  region = var.gcp_region
}

# Service account
resource "google_service_account" "keycloak" {
  account_id   = "keycloak-sa"
  display_name = "Keycloak Service Account"
}

# Logging role for service account
resource "google_project_iam_member" "keycloak_log_writer" {
  project = var.gcp_project_id
  role    = "roles/logging.logWriter"
  member  = "serviceAccount:${google_service_account.keycloak.email}"
}

# Monitoring role for service account
resource "google_project_iam_member" "keycloak_metric_writer" {
  project = var.gcp_project_id
  role    = "roles/monitoring.metricWriter"
  member  = "serviceAccount:${google_service_account.keycloak.email}"
}

# VM instance
resource "google_compute_instance" "keycloak" {
  name         = "keycloak-server"
  machine_type = "e2-small"
  zone         = "${var.gcp_region}-${var.gcp_zone}"
  tags         = ["keycloak", "https-server"]

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
      size  = 50
      type  = "pd-standard"
    }
  }

  network_interface {
    network    = "default"
    network_ip = var.keycloak_internal_ip
    access_config {
      nat_ip = google_compute_address.keycloak_static_ip.address
    }
  }

  service_account {
    email  = google_service_account.keycloak.email
    scopes = ["cloud-platform"]
  }

  metadata = {
    enable-oslogin = "TRUE"
  }

  metadata_startup_script = base64encode(templatefile(
    "${path.module}/keycloak-startup.sh",
    {
      keycloak_admin_user     = var.keycloak_admin_user
      keycloak_admin_password = var.keycloak_admin_password
      keycloak_domain         = var.keycloak_domain
      letsencrypt_email       = var.letsencrypt_email
      db_host                 = var.db_host
      db_port                 = var.db_port
      db_name                 = var.db_name
      db_username             = var.db_username
      db_password             = var.db_password
    }
  ))

  depends_on = [
    google_compute_firewall.keycloak_http,
    google_compute_firewall.keycloak_https,
    google_compute_firewall.keycloak_ssh
  ]
}

# Cloud SQL instance (PostgreSQL) for Keycloak
resource "google_sql_database_instance" "keycloak" {
  name             = "keycloak-db"
  database_version = "POSTGRES_15"
  region           = var.gcp_region

  settings {
    tier              = "db-f1-micro"
    availability_type = "REGIONAL"
    backup_configuration {
      enabled                        = true
      start_time                     = "03:00"
      transaction_log_retention_days = 7
    }

    ip_configuration {
      require_ssl = true
      authorized_networks {
        name  = "keycloak-vm"
        value = google_compute_address.keycloak_static_ip.address
      }
    }

    database_flags {
      name  = "cloudsql_iam_authentication"
      value = "on"
    }
  }

  deletion_protection = true
}

# Keycloak database
resource "google_sql_database" "keycloak" {
  name     = var.db_name
  instance = google_sql_database_instance.keycloak.name
}

# Keycloak database user
resource "google_sql_user" "keycloak" {
  name     = var.db_username
  instance = google_sql_database_instance.keycloak.name
  password = var.db_password
}

# DNS record
resource "google_dns_record_set" "keycloak" {
  name         = "${var.keycloak_domain}."
  type         = "A"
  ttl          = 300
  managed_zone = var.dns_managed_zone_name
  rrdatas      = [google_compute_address.keycloak_static_ip.address]
}

# Outputs
output "keycloak_static_ip" {
  value       = google_compute_address.keycloak_static_ip.address
  description = "Static IP address of Keycloak VM"
}

output "keycloak_domain" {
  value       = var.keycloak_domain
  description = "Domain of Keycloak server"
}

output "keycloak_admin_console_url" {
  value       = "https://${var.keycloak_domain}/admin"
  description = "Keycloak Admin Console URL"
}

output "keycloak_realm_url" {
  value       = "https://${var.keycloak_domain}/realms/train-gym"
  description = "Keycloak Realm URL for TrAIn application"
}

output "database_host" {
  value       = google_sql_database_instance.keycloak.private_ip_address
  description = "Database host address"
}
