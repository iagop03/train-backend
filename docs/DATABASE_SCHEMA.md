# TrAIn - Database Schema Documentation

## Overview
Completo esquema PostgreSQL para el proyecto TrAIn (AI gym tracker) con soporte para usuarios, entrenadores, ejercicios, rutinas, seguimiento de desempeño y más.

## Tablas Principales

### 1. muscle_groups
Categorías de grupos musculares utilizadas en ejercicios.

**Campos:**
- `id` (UUID): Identificador único
- `name` (VARCHAR): Nombre del grupo muscular
- `description` (TEXT): Descripción detallada
- `image_url` (VARCHAR): URL de la imagen representativa
- `created_at`, `updated_at` (TIMESTAMP): Auditoría de datos

---

### 2. gyms
Información de gimnasios registrados en la plataforma.

**Campos:**
- `id` (UUID): Identificador único
- `name` (VARCHAR): Nombre del gimnasio
- `address`, `city`, `state`, `country`, `postal_code` (VARCHAR): Dirección completa
- `latitude`, `longitude` (DECIMAL): Coordenadas geográficas
- `phone`, `email`, `website` (VARCHAR): Datos de contacto
- `logo_url` (VARCHAR): URL del logo
- `is_active` (BOOLEAN): Estado del gimnasio

---

### 3. users
Usuarios registrados en la plataforma.

**Campos:**
- `id` (UUID): Identificador único
- `keycloak_id` (VARCHAR): ID externo de Keycloak para autenticación
- `email` (VARCHAR): Email único
- `first_name`, `last_name` (VARCHAR): Nombre completo
- `phone` (VARCHAR): Teléfono
- `date_of_birth` (DATE): Fecha de nacimiento
- `gender` (VARCHAR): Género
- `profile_picture_url` (VARCHAR): URL de foto de perfil
- `bio` (TEXT): Biografía
- `height_cm`, `weight_kg` (DECIMAL): Medidas físicas
- `gym_id` (UUID): Referencia al gimnasio afiliado
- `is_trainer` (BOOLEAN): Indica si es entrenador
- `is_active` (BOOLEAN): Estado de la cuenta
- `last_login` (TIMESTAMP): Último acceso

---

### 4. trainers
Información detallada de entrenadores certificados.

**Campos:**
- `id` (UUID): Identificador único
- `user_id` (UUID): Referencia al usuario (relación 1:1)
- `specialization` (VARCHAR): Especialización del entrenador
- `certification_number` (VARCHAR): Número de certificación
- `certification_expiry_date` (DATE): Fecha de vencimiento
- `experience_years` (INT): Años de experiencia
- `rating` (DECIMAL): Calificación promedio
- `hourly_rate` (DECIMAL): Tarifa por hora
- `is_available` (BOOLEAN): Disponibilidad
- `total_clients` (INT): Cantidad de clientes
- `gym_id` (UUID): Gimnasio donde trabaja
- `verified` (BOOLEAN): Verificación por administrador

---

### 5. exercises
Catálogo de ejercicios disponibles.

**Campos:**
- `id` (UUID): Identificador único
- `name` (VARCHAR): Nombre del ejercicio
- `description` (TEXT): Descripción
- `muscle_group_id` (UUID): Grupo muscular (FK)
- `equipment_required` (VARCHAR): Equipamiento necesario
- `difficulty_level` (VARCHAR): Nivel de dificultad (Principiante/Intermedio/Avanzado)
- `video_url` (VARCHAR): URL del video instructivo
- `image_url` (VARCHAR): URL de imagen
- `instructions` (TEXT): Instrucciones detalladas
- `is_active` (BOOLEAN): Disponible en la plataforma

---

### 6. routines
Rutinas de ejercicios asignadas a usuarios.

**Campos:**
- `id` (UUID): Identificador único
- `user_id` (UUID): Usuario propietario (FK)
- `trainer_id` (UUID): Entrenador asignado (FK, opcional)
- `name` (VARCHAR): Nombre de la rutina
- `description` (TEXT): Descripción
- `goal` (VARCHAR): Objetivo de la rutina
- `difficulty_level` (VARCHAR): Nivel de dificultad
- `duration_weeks` (INT): Duración en semanas
- `is_template` (BOOLEAN): Es una plantilla reutilizable
- `is_active` (BOOLEAN): Actualmente en uso
- `start_date`, `end_date` (DATE): Período de la rutina

---

### 7. routine_days
Días específicos dentro de una rutina.

**Campos:**
- `id` (UUID): Identificador único
- `routine_id` (UUID): Rutina padre (FK)
- `day_of_week` (INT): Día de la semana (0-6)
- `day_name` (VARCHAR): Nombre del día
- `is_rest_day` (BOOLEAN): Día de descanso
- `notes` (TEXT): Notas especiales
- `rest_duration_seconds` (INT): Descanso entre series

---

### 8. routine_exercises
Ejercicios asignados a cada día de rutina.

**Campos:**
- `id` (UUID): Identificador único
- `routine_day_id` (UUID): Día de rutina (FK)
- `exercise_id` (UUID): Ejercicio (FK)
- `sets` (INT): Número de series
- `reps` (INT): Repeticiones por serie
- `weight_kg` (DECIMAL): Peso recomendado
- `duration_seconds` (INT): Duración del ejercicio
- `rest_seconds` (INT): Descanso después del ejercicio
- `notes` (TEXT): Notas adicionales
- `order_in_day` (INT): Orden de ejecución

---

### 9. subscriptions
Suscripciones de usuarios a gimnasios.

**Campos:**
- `id` (UUID): Identificador único
- `user_id` (UUID): Usuario (FK)
- `gym_id` (UUID): Gimnasio (FK)
- `subscription_type` (VARCHAR): Tipo de suscripción (MONTHLY/QUARTERLY/YEARLY)
- `status` (VARCHAR): Estado (ACTIVE/SUSPENDED/EXPIRED)
- `start_date`, `end_date` (TIMESTAMP): Período
- `renewal_date` (TIMESTAMP): Próxima renovación
- `price` (DECIMAL): Precio
- `currency` (VARCHAR): Moneda
- `payment_method` (VARCHAR): Método de pago
- `auto_renewal` (BOOLEAN): Renovación automática

---

### 10. messages
Sistema de mensajería entre usuarios.

**Campos:**
- `id` (UUID): Identificador único
- `sender_id` (UUID): Usuario que envía (FK)
- `recipient_id` (UUID): Usuario que recibe (FK)
- `subject` (VARCHAR): Asunto
- `body` (TEXT): Contenido del mensaje
- `message_type` (VARCHAR): Tipo (CHAT/NOTIFICATION/ALERT)
- `is_read` (BOOLEAN): Leído
- `read_at` (TIMESTAMP): Fecha de lectura
- `parent_message_id` (UUID): Para hilos de conversación
- `attachment_url` (VARCHAR): URL de adjunto

---

### 11. workout_sessions
Sesiones de entrenamiento registradas por usuarios.

**Campos:**
- `id` (UUID): Identificador único
- `user_id` (UUID): Usuario (FK)
- `routine_id`, `routine_day_id` (UUID): Referencia a la rutina
- `start_time`, `end_time` (TIMESTAMP): Duración de la sesión
- `duration_minutes` (INT): Duración total
- `notes` (TEXT): Notas personales
- `mood_before`, `mood_after` (INT): Escala del humor (1-10)
- `calories_burned` (DECIMAL): Calorías quemadas
- `intensity_level` (INT): Nivel de intensidad
- `is_completed` (BOOLEAN): Completada

---

### 12. exercise_logs
Detalles de cada ejercicio ejecutado en una sesión.

**Campos:**
- `id` (UUID): Identificador único
- `workout_session_id` (UUID): Sesión (FK)
- `exercise_id` (UUID): Ejercicio (FK)
- `sets_completed`, `reps_completed` (INT): Realizado
- `weight_used_kg` (DECIMAL): Peso utilizado
- `difficulty_rating` (INT): Calificación de dificultad
- `is_completed` (BOOLEAN): Completado

---

### 13. personal_records
Méjores marcas personales del usuario.

**Campos:**
- `id` (UUID): Identificador único
- `user_id` (UUID): Usuario (FK)
- `exercise_id` (UUID): Ejercicio (FK)
- `weight_kg` (DECIMAL): Peso máximo
- `reps` (INT): Repeticiones
- `date_achieved` (DATE): Fecha del logro

---

### 14. body_measurements
Medidas corporales históricas.

**Campos:**
- `id` (UUID): Identificador único
- `user_id` (UUID): Usuario (FK)
- `measurement_date` (DATE): Fecha de medición
- `weight_kg`, `body_fat_percentage` (DECIMAL): Medidas
- `chest_cm`, `waist_cm`, `hip_cm`, etc. (DECIMAL): Circunferencias

---

## Tablas de Relación

### user_followers
Relación de seguimiento entre usuarios (social network).

### trainer_clients
Relación many-to-many entre entrenadores y clientes.

---

## Vistas Principales

### active_users
Vista de usuarios activos en la plataforma.

### trainer_statistics
Estadísticas detalladas de entrenadores verificados con número de clientes y rutinas.

### user_routine_overview
Resumen de rutinas del usuario con conteo de días y ejercicios.

### user_progress_summary
Resumen de progreso del usuario incluyendo sesiones completadas, minutos totales y récords personales.

---

## Índices Clave

- Búsquedas por usuario: `idx_users_email`, `idx_users_keycloak_id`
- Búsquedas por gimnasio: `idx_subscriptions_gym_id`, `idx_gyms_city`
- Búsquedas temporales: `idx_messages_created_at`, `idx_workout_sessions_start_time`
- Búsquedas de estado: `idx_routines_is_active`, `idx_subscriptions_status`

---

## Restricciones de Integridad

1. **Relaciones de borrado:**
   - Cascada: Mensajes, sesiones de entrenamiento, logs de ejercicio
   - SET NULL: Gimnasio de usuario, entrenador de rutina
   - RESTRICT: Grupos musculares en ejercicios

2. **Restricciones únicas:**
   - Email de usuario
   - Keycloak ID
   - Email de gimnasio
   - Una suscripción activa por usuario-gimnasio

3. **Validaciones:**
   - `follower_id != following_id` en user_followers
   - Verificación de fechas de subscripción

---

## Migración con Flyway

Los scripts se ejecutan automáticamente al iniciar la aplicación:
- `V1__initial_schema.sql`: Creación de tablas base
- `V2__insert_initial_data.sql`: Datos de ejemplo
- `V3__create_performance_tracking.sql`: Tablas de seguimiento