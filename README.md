# TrAIn Backend

Spring Boot 3 backend para TrAIn - AI Gym Tracker.

## Stack Tecnológico

- **Java 21**
- **Spring Boot 3.x**
- **PostgreSQL Cloud SQL**
- **MongoDB Atlas**
- **Keycloak** (Autenticación/Autorización)
- **Google Cloud Platform**

## Prerequisitos

- JDK 21+
- Maven 3.8+
- PostgreSQL 14+
- MongoDB 5.0+
- Keycloak 22+

## Instalación

```bash
# Clonar repositorio
git clone https://github.com/iagop03/train-backend.git
cd train-backend

# Configurar variables de entorno
cp .env.example .env

# Build
mvn clean package

# Ejecutar
java -jar target/train-backend.jar
```

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/train/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── config/
│   │   ├── security/
│   │   └── exception/
│   └── resources/
│       ├── application.yml
│       └── db/migration/
└── test/
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

### Users
- `GET /api/v1/users/{id}`
- `PUT /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`

## Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn test -Pintegration

# Coverage
mvn jacoco:report
```

## Contributing

1. Crear feature branch: `git checkout -b feature/TRAIN-XXX`
2. Commit changes: `git commit -am 'feat: descripción'`
3. Push a branch: `git push origin feature/TRAIN-XXX`
4. Abrir Pull Request

## License

MIT License