# Guía de Contribución - TrAIn Backend

## Inicio rápido

### Requisitos
- JDK 21+
- Maven 3.8+
- PostgreSQL 16
- Docker & Docker Compose (opcional)

### Setup local

```bash
# Clonar repo
git clone https://github.com/iagop03/train-backend.git
cd train-backend

# Setup base de datos (Docker)
docker-compose up -d postgres

# Build
mvn clean install

# Run
mvn spring-boot:run
```

## Workflow de desarrollo

1. **Crear rama**: `git checkout -b feature/TRAIN-XXX-descripcion`
2. **Hacer cambios**: Seguir estilo de código (Google Java Style Guide)
3. **Tests**: Asegurar cobertura >80%
4. **Commit**: `git commit -m "TRAIN-XXX: descripción"`
5. **Push**: `git push origin feature/TRAIN-XXX-descripcion`
6. **PR**: Crear Pull Request con template

## Convenciones de código

### Naming
- Clases: PascalCase
- Métodos/variables: camelCase
- Constantes: UPPER_SNAKE_CASE

### Estructura
- Controllers: `@RestController`, `@RequestMapping`
- Services: `@Service`, lógica de negocio
- Repositories: `@Repository`, Spring Data JPA
- DTOs: Separar en carpeta `dto`

### Ejemplo

```java
@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;
    
    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }
    
    @GetMapping
    public ResponseEntity<List<WorkoutDTO>> getWorkouts() {
        return ResponseEntity.ok(workoutService.findAll());
    }
}
```

## Testing

```bash
# Run all tests
mvn test

# Coverage report
mvn jacoco:report
open target/site/jacoco/index.html
```

## Branch Protection Rules

**main** & **develop**:
- ✅ Require PR reviews (2 approvals)
- ✅ Require status checks (CI/CD, SonarQube, tests)
- ✅ Require branches up to date
- ✅ Dismiss stale reviews
- ✅ Require signed commits
- ❌ Allow force pushes
- ❌ Allow deletions

## Commit Messages

```
TRAIN-XXX: Brief description

Optional detailed explanation:
- What was changed
- Why it was changed
- Any relevant context
```

## Issues & PRs

- Linkar siempre al issue: `Fix #XXX`
- Usar labels: `bug`, `feature`, `documentation`, `help wanted`
- Asignar milestone para versión target

## Code Review

Al revisar PRs:
1. Verificar funcionalidad
2. Revisar tests
3. Validar convenciones
4. Sugerir mejoras
5. Aprobar o solicitar cambios

## Release

```bash
mvn release:prepare
mvn release:perform
```

## Soporte

- Documentación: `/docs`
- Issues: GitHub Issues
- Discussions: GitHub Discussions
