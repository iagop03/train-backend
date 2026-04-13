# Terraform configuration for MongoDB Atlas M0 Cluster and VPC Peering

terraform {
  required_providers {
    mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 1.14"
    }
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

variable "mongodb_atlas_org_id" {
  type        = string
  description = "MongoDB Atlas Organization ID"
}

variable "mongodb_atlas_api_key" {
  type        = string
  sensitive   = true
  description = "MongoDB Atlas API Key"
}

variable "gcp_project_id" {
  type        = string
  description = "GCP Project ID"
}

variable "gcp_region" {
  type        = string
  default     = "us-central1"
  description = "GCP Region"
}

variable "atlas_region" {
  type        = string
  default     = "US_CENTRAL_1"
  description = "MongoDB Atlas Region"
}

# MongoDB Atlas Provider Configuration
provider "mongodbatlas" {
  org_id      = var.mongodb_atlas_org_id
  private_key = var.mongodb_atlas_api_key
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

# Create MongoDB Atlas Project
resource "mongodbatlas_project" "train_project" {
  org_id = var.mongodb_atlas_org_id
  name   = "train-ai-gym-tracker"
}

# Create MongoDB Atlas M0 Cluster
resource "mongodbatlas_cluster" "train_cluster" {
  project_id     = mongodbatlas_project.train_project.id
  name           = "train-m0-cluster"
  cluster_type   = "REPLICASET"
  replication_factor = 3
  
  provider_name               = "MONGODB"
  provider_instance_size_name = "M0"
  provider_region_name        = var.atlas_region
  
  # Backup settings for M0
  backup_enabled = true
  
  # Encryption at rest
  encryption_at_rest_provider = "NONE" # M0 doesn't support customer-managed encryption
  
  # Disable auto scaling for M0
  auto_scaling_disk_gb_enabled = false
  
  # Enable network peering
  tags = {
    Environment = "production"
    Project     = "TrAIn"
  }
}

# Create MongoDB Atlas VPC
resource "mongodbatlas_network_container" "train_vpc" {
  project_id           = mongodbatlas_project.train_project.id
  atlas_cidr_block     = "10.0.0.0/21"
  provider_name        = "GCP"
  region_name          = var.atlas_region
  depends_on           = [mongodbatlas_cluster.train_cluster]
}

# Get GCP VPC network
data "google_compute_network" "train_vpc_gcp" {
  name = "default"
}

# Create GCP VPC peering connection (initiate from MongoDB Atlas)
resource "mongodbatlas_network_peering" "train_peering" {
  project_id              = mongodbatlas_project.train_project.id
  container_id            = mongodbatlas_network_container.train_vpc.id
  provider_name           = "GCP"
  gcp_project_id          = var.gcp_project_id
  network_name            = data.google_compute_network.train_vpc_gcp.name
  
  depends_on = [mongodbatlas_network_container.train_vpc]
}

# Accept GCP VPC peering connection
resource "google_compute_network_peering" "train_peering_gcp" {
  name                 = "train-mongodb-atlas-peering"
  network              = data.google_compute_network.train_vpc_gcp.self_link
  peer_project         = mongodbatlas_network_peering.train_peering.gcp_project_id
  
  # Auto create routes
  auto_create_routes = true
  
  depends_on = [mongodbatlas_network_peering.train_peering]
}

# IP Whitelist - GCP Cloud SQL
resource "mongodbatlas_project_ip_accesslist" "gcp_cloud_sql" {
  project_id = mongodbatlas_project.train_project.id
  comment    = "GCP Cloud SQL Instance"
  ip_address = google_sql_database_instance.train_postgres.public_ip_address
}

# IP Whitelist - GCP Compute Engine (Spring Boot instances)
resource "mongodbatlas_project_ip_accesslist" "gcp_compute_engine" {
  project_id = mongodbatlas_project.train_project.id
  comment    = "GCP Compute Engine - TrAIn Backend"
  cidr_block = google_compute_network.train_vpc_gcp.auto_create_subnetworks ? "0.0.0.0/0" : "10.0.0.0/8"
}

# Get GCP Cloud SQL instance (referenced for IP)
data "google_sql_database_instance" "train_postgres" {
  name = google_sql_database_instance.train_postgres.name
}

resource "google_sql_database_instance" "train_postgres" {
  name             = "train-postgres-db"
  database_version = "POSTGRES_15"
  region           = var.gcp_region
  
  settings {
    tier                  = "db-custom-2-8192"
    availability_type     = "REGIONAL"
    backup_configuration {
      enabled            = true
      binary_log_enabled = true
    }
  }
  
  depends_on = [mongodbatlas_network_peering.train_peering]
}

# MongoDB Atlas Database User
resource "mongodbatlas_database_user" "train_user" {
  project_id         = mongodbatlas_project.train_project.id
  auth_database_name = "admin"
  username           = "train_app_user"
  password           = random_password.mongodb_password.result
  
  roles {
    role_name     = "readWrite"
    database_name = "train_db"
  }
  
  depends_on = [mongodbatlas_cluster.train_cluster]
}

# Generate random password for MongoDB user
resource "random_password" "mongodb_password" {
  length  = 32
  special = true
}

# Store MongoDB credentials in GCP Secret Manager
resource "google_secret_manager_secret" "mongodb_uri" {
  secret_id = "train-mongodb-uri"
  
  replication {
    user_managed {
      replicas {
        location = var.gcp_region
      }
    }
  }
}

resource "google_secret_manager_secret_version" "mongodb_uri" {
  secret      = google_secret_manager_secret.mongodb_uri.id
  secret_data = "mongodb+srv://${mongodbatlas_database_user.train_user.username}:${random_password.mongodb_password.result}@${mongodbatlas_cluster.train_cluster.connection_strings[0].standard_srv}"
}

resource "google_secret_manager_secret" "mongodb_password" {
  secret_id = "train-mongodb-password"
  
  replication {
    user_managed {
      replicas {
        location = var.gcp_region
      }
    }
  }
}

resource "google_secret_manager_secret_version" "mongodb_password" {
  secret      = google_secret_manager_secret.mongodb_password.id
  secret_data = random_password.mongodb_password.result
}

# Outputs
output "mongodb_atlas_project_id" {
  value       = mongodbatlas_project.train_project.id
  description = "MongoDB Atlas Project ID"
}

output "mongodb_cluster_name" {
  value       = mongodbatlas_cluster.train_cluster.name
  description = "MongoDB Cluster Name"
}

output "mongodb_connection_string" {
  value       = mongodbatlas_cluster.train_cluster.connection_strings[0].standard_srv
  description = "MongoDB Atlas Connection String (SRV)"
  sensitive   = true
}

output "mongodb_user" {
  value       = mongodbatlas_database_user.train_user.username
  description = "MongoDB Database User"
}

output "vpc_peering_status" {
  value       = mongodbatlas_network_peering.train_peering.status
  description = "VPC Peering Status"
}

output "gcp_peering_connection" {
  value       = google_compute_network_peering.train_peering_gcp.name
  description = "GCP Peering Connection Name"
}