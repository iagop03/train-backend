# TrAIn - AI Gym Tracker Backend

## Descripción
Backend para la aplicación TrAIn (AI Gym Tracker), desarrollado con Spring Boot 3 y Java 21. Proporciona APIs REST para gestionar entrenamientos, ejercicios, usuarios y análisis de IA.

## Stack Tecnológico
- **Java 21**
- **Spring Boot 3.x**
- **PostgreSQL** (Cloud SQL)
- **MongoDB Atlas**
- **Keycloak** (Autenticación)
- **GCP** (Hosting)

## Requisitos Previos
- JDK 21
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 16+

## Instalación Local

```bash
# Clonar repositorio
git clone https://github.com/iagop03/train-backend.git
cd train-backend

# Instalar dependencias
mvn clean install

# Configurar variables de entorno
cp .env.example .env

# Iniciar base de datos
docker-compose up -d

# Ejecutar aplicación
mvn spring-boot:run
```

## Estructura del Proyecto
```
train-backend/
├── src/main/java/com/train/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── config/
│   └── exception/
├── src/test/
├── src/main/resources/
│   ├── application.yml
│   └── application-{profile}.yml
└── docker-compose.yml
```

## Variables de Entorno
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/train_db
SPRING_DATASOURCE_USERNAME=train_user
SPRING_DATASOURCE_PASSWORD=train_pass
MONGODB_URI=mongodb+srv://...
KEYCLOAK_SERVER_URL=https://keycloak.example.com
GCP_PROJECT_ID=your-project-id
```

## Testing
```bash
# Ejecutar todos los tests
mvn test

# Ejecutar con cobertura
mvn test jacoco:report

# Ver reporte
open target/site/jacoco/index.html
```

## API Documentation
La documentación Swagger está disponible en: `http://localhost:8080/swagger-ui.html`

## Deployment
Ver [DEPLOYMENT.md](./docs/DEPLOYMENT.md) para instrucciones de deployment a GCP.

## Contribución
Por favor revisa [CONTRIBUTING.md](./CONTRIBUTING.md) antes de hacer cambios.

## Licencia
MIT
