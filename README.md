# TrAIn Backend

Backend para la aplicación TrAIn (AI gym tracker) - Rastreador de ejercicios con IA.

## Tech Stack

- **Java 21**
- **Spring Boot 3.x**
- **Spring Security + Keycloak** (Autenticación)
- **PostgreSQL** (Cloud SQL)
- **MongoDB Atlas** (Datos no estructurados)
- **GCP** (Cloud Platform)
- **Maven** (Build tool)

## Requisitos previos

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+
- MongoDB 6+

## Instalación

```bash
# Clonar repositorio
git clone https://github.com/iagop03/train-backend.git
cd train-backend

# Instalar dependencias
mvn clean install

# Ejecutar con Docker Compose
docker-compose up -d

# Ejecutar aplicación
mvn spring-boot:run
```

## Estructura del proyecto

```
src/
├── main/
│   ├── java/com/train/
│   │   ├── api/
│   │   ├── domain/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── config/
│   │   ├── security/
│   │   └── exception/
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
└── test/
    └── java/com/train/
```

## Scripts disponibles

```bash
# Tests
mvn test                    # Ejecutar tests unitarios
mvn verify                  # Ejecutar tests + análisis

# Build
mvn clean package           # Build del proyecto
mvn spring-boot:run         # Ejecutar en desarrollo

# Docker
docker-compose up           # Iniciar infraestructura
docker-compose down         # Parar infraestructura
```

## Documentación de API

La documentación Swagger está disponible en:
```
http://localhost:8080/swagger-ui.html
```

## Contribuir

Ver [CONTRIBUTING.md](CONTRIBUTING.md)

## Licencia

MIT
