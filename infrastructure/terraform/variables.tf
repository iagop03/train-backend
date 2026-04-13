variable "gcp_project_id" {
  type        = string
  description = "GCP Project ID"
}

variable "gcp_region" {
  type        = string
  description = "GCP Region"
  default     = "us-central1"
}

variable "authorized_networks" {
  type = list(object({
    name = string
    cidr = string
  }))
  description = "List of authorized networks for Cloud SQL (dev/staging only)"
  default = [
    {
      name = "office"
      cidr = "203.0.113.0/24" # Replace with actual office IP
    }
  ]
}
