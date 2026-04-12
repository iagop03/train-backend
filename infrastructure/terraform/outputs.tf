output "cloudsql_instance_name" {
  value       = google_sql_database_instance.train_postgres.name
  description = "Cloud SQL Instance name"
}

output "cloudsql_private_ip" {
  value       = google_sql_database_instance.train_postgres.private_ip_address
  description = "Cloud SQL private IP address"
}

output "cloudsql_connection_name" {
  value       = google_sql_database_instance.train_postgres.connection_name
  description = "Cloud SQL connection name"
}

output "db_root_password" {
  value       = random_password.db_root_password.result
  sensitive   = true
  description = "Database root password"
}

output "ssl_cert" {
  value       = google_sql_ssl_cert.train_ssl_cert.cert
  sensitive   = true
  description = "SSL certificate"
}

output "ssl_cert_key" {
  value       = google_sql_ssl_cert.train_ssl_cert.private_key
  sensitive   = true
  description = "SSL certificate private key"
}

output "ssl_server_ca_cert" {
  value       = google_sql_ssl_cert.train_ssl_cert.server_ca_cert
  sensitive   = true
  description = "SSL server CA certificate"
}

output "vpc_network_name" {
  value       = google_compute_network.private_network.name
  description = "VPC network name"
}

output "backup_bucket_name" {
  value       = google_storage_bucket.backups.name
  description = "Cloud Storage bucket for backups"
}

output "service_account_email" {
  value       = google_service_account.train_app.email
  description = "Service account email for application"
}
