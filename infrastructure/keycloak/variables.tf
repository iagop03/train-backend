# Terraform variables for Keycloak deployment
# TRAIN-16: GCP e2-small VM configuration

variable "gcp_project_id" {
  description = "GCP Project ID"
  type        = string
}

variable "gcp_region" {
  description = "GCP region"
  type        = string
  default     = "us-central1"
}

variable "gcp_zone" {
  description = "GCP zone letter (a, b, c, etc.)"
  type        = string
  default     = "a"
}

variable "keycloak_admin_user" {
  description = "Keycloak admin username"
  type        = string
  default     = "admin"
}

variable "keycloak_admin_password" {
  description = "Keycloak admin password"
  type        = string
  sensitive   = true
}

variable "keycloak_domain" {
  description = "Domain name for Keycloak"
  type        = string
}

variable "keycloak_internal_ip" {
  description = "Internal IP address for Keycloak VM"
  type        = string
  default     = "10.128.0.2"
}

variable "db_host" {
  description = "Database host"
  type        = string
}

variable "db_port" {
  description = "Database port"
  type        = number
  default     = 5432
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "keycloak"
}

variable "db_username" {
  description = "Database username"
  type        = string
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "letsencrypt_email" {
  description = "Email for Let's Encrypt SSL certificate"
  type        = string
}

variable "allowed_ssh_cidrs" {
  description = "CIDR blocks allowed for SSH access"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "dns_managed_zone_name" {
  description = "GCP Cloud DNS managed zone name"
  type        = string
}
