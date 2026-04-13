variable "gcp_project_id" {
  description = "GCP Project ID"
  type        = string
  default     = "train-ai-gym-tracker"
}

variable "gcp_region" {
  description = "GCP Region"
  type        = string
  default     = "us-central1"
}

variable "gcp_zone" {
  description = "GCP Zone"
  type        = string
  default     = "us-central1-a"
}

variable "vpc_network_name" {
  description = "VPC Network Name"
  type        = string
  default     = "train-vpc"
}

variable "subnet_cidr" {
  description = "Subnet CIDR Range"
  type        = string
  default     = "10.0.0.0/20"
}

variable "cloud_sql_instance_name" {
  description = "Cloud SQL Instance Name"
  type        = string
  default     = "train-postgres-primary"
}

variable "cloud_sql_database_version" {
  description = "PostgreSQL Version"
  type        = string
  default     = "POSTGRES_15"
}

variable "cloud_sql_tier" {
  description = "Cloud SQL Machine Type"
  type        = string
  default     = "db-f1-micro"
}

variable "cloud_sql_disk_size" {
  description = "Cloud SQL Disk Size in GB"
  type        = number
  default     = 50
}

variable "database_name" {
  description = "Database Name"
  type        = string
  default     = "train_db"
}

variable "database_user" {
  description = "Database Application User"
  type        = string
  default     = "app_user"
}

variable "cloud_run_service_name" {
  description = "Cloud Run Service Name"
  type        = string
  default     = "train-backend"
}

variable "cloud_run_memory" {
  description = "Cloud Run Memory Allocation"
  type        = string
  default     = "2Gi"
}

variable "cloud_run_cpu" {
  description = "Cloud Run CPU Allocation"
  type        = string
  default     = "2"
}

variable "cloud_run_timeout" {
  description = "Cloud Run Timeout in Seconds"
  type        = number
  default     = 3600
}

variable "container_registry" {
  description = "Container Registry"
  type        = string
  default     = "gcr.io"
}

variable "container_image_name" {
  description = "Container Image Name"
  type        = string
  default     = "train-backend"
}

variable "container_image_tag" {
  description = "Container Image Tag"
  type        = string
  default     = "latest"
}

variable "github_owner" {
  description = "GitHub Repository Owner"
  type        = string
  default     = "iagop03"
}

variable "github_repo" {
  description = "GitHub Repository Name"
  type        = string
  default     = "train-backend"
}

variable "github_branch" {
  description = "GitHub Branch for Triggers"
  type        = string
  default     = "main"
}
