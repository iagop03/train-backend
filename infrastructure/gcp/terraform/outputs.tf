output "gcp_project_id" {
  description = "GCP Project ID"
  value       = data.google_project.project.project_id
}

output "vpc_network_id" {
  description = "VPC Network ID"
  value       = google_compute_network.main.id
}

output "vpc_network_name" {
  description = "VPC Network Name"
  value       = google_compute_network.main.name
}

output "subnetwork_id" {
  description = "Subnetwork ID"
  value       = google_compute_subnetwork.main.id
}

output "cloud_sql_instance_connection_name" {
  description = "Cloud SQL Instance Connection Name (for Cloud Run)"
  value       = google_sql_database_instance.postgres.connection_name
}

output "cloud_sql_private_ip_address" {
  description = "Cloud SQL Private IP Address"
  value       = google_sql_database_instance.postgres.private_ip_address
}

output "cloud_sql_public_ip_address" {
  description = "Cloud SQL Public IP Address"
  value       = google_sql_database_instance.postgres.public_ip_address
}

output "database_name" {
  description = "Created Database Name"
  value       = google_sql_database.train_db.name
}

output "database_user" {
  description = "Application Database User"
  value       = google_sql_user.app_user.name
}

output "cloud_run_service_url" {
  description = "Cloud Run Service URL"
  value       = google_cloud_run_service.backend.status[0].url
}

output "cloud_run_service_name" {
  description = "Cloud Run Service Name"
  value       = google_cloud_run_service.backend.name
}

output "cloud_run_service_account_email" {
  description = "Cloud Run Service Account Email"
  value       = google_service_account.cloud_run.email
}

output "cloud_build_service_account_email" {
  description = "Cloud Build Service Account Email"
  value       = google_service_account.cloud_build.email
}

output "cloud_build_trigger_name" {
  description = "Cloud Build Trigger Name"
  value       = google_cloudbuild_trigger.backend.name
}
