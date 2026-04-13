# TrAIn Backend

Backend para la aplicación TrAIn (AI Gym Tracker) construido con Spring Boot 3 y Java 21.

## Stack Tecnológico

- **Java 21**
- **Spring Boot 3**
- **PostgreSQL Cloud SQL**
- **MongoDB Atlas**
- **Keycloak** (Autenticación)
- **GCP** (Cloud Platform)
- **Maven** (Build tool)

## Requisitos Previos

- JDK 21+
- Maven 3.8+
- Docker (opcional)
- PostgreSQL 15+

## Configuración Local

### 1. Clonar el repositorio
```bash
git clone https://github.com/iagop03/train-backend.git
cd train-backend
```

### 2. Variables de Entorno
```bash
cp .env.example .env
# Editar .env con tus valores
```

### 3. Construir el proyecto
```bash
mvn clean install
```

### 4. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/train/
│   │   ├── config/          # Configuraciones
│   │   ├── controller/       # REST Controllers
│   │   ├── service/          # Lógica de negocio
│   │   ├── repository/       # Acceso a datos
│   │   ├── entity/           # Entidades JPA
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── exception/        # Excepciones personalizadas
│   │   └── security/         # Configuración de seguridad
│   └── resources/
│       ├── application.yml   # Configuración principal
│       └── application-*.yml # Configuraciones por ambiente
└── test/
    └── java/com/train/      # Tests unitarios e integración
```

## API Endpoints

### Autenticación
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/logout` - Logout
- `POST /api/v1/auth/refresh` - Refresh token

### Usuarios
- `GET /api/v1/users/{id}` - Obtener usuario
- `PUT /api/v1/users/{id}` - Actualizar usuario
- `DELETE /api/v1/users/{id}` - Eliminar usuario

### Entrenamientos
- `GET /api/v1/workouts` - Listar entrenamientos
- `POST /api/v1/workouts` - Crear entrenamiento
- `GET /api/v1/workouts/{id}` - Obtener entrenamiento
- `PUT /api/v1/workouts/{id}` - Actualizar entrenamiento
- `DELETE /api/v1/workouts/{id}` - Eliminar entrenamiento

## Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest=NombreClaseTest

# Con coverage
mvn test jacoco:report
```

## Contribuir

1. Crear una rama: `git checkout -b feature/TRAIN-XXX`
2. Hacer cambios y commits: `git commit -am 'Add feature'`
3. Push a la rama: `git push origin feature/TRAIN-XXX`
4. Crear Pull Request

## Branch Protection

Las siguientes reglas están configuradas en `main` y `develop`:
- Requerir pull request reviews (mínimo 2)
- Requerir que los checks pasen antes de mergear
- Requerir commits signados
- Descartar aprobaciones obsoletas
- Requerir actualización antes de mergear

## Documentación

- [API Docs](docs/API.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Contributing Guide](CONTRIBUTING.md)

## Licencia

MIT
