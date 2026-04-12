variable "gcp_project_id" {
  description = "GCP Project ID"
  type        = string
  sensitive   = true
}

variable "gcp_region" {
  description = "GCP Region"
  type        = string
  default     = "us-central1"
}

variable "office_ip_cidr" {
  description = "Office IP CIDR for authorized networks"
  type        = string
  sensitive   = true
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}
