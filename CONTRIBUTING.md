# Guía de Contribución - TrAIn Backend

## Código de Conducta
Sé respetuoso y constructivo con otros colaboradores.

## Proceso de Contribución

### 1. Fork y Clone
```bash
git clone https://github.com/iagop03/train-backend.git
cd train-backend
```

### 2. Crear rama
```bash
git checkout -b feature/TRAIN-XXX-descripcion
```

Nombrado de ramas:
- `feature/TRAIN-XXX-descripcion` - Nueva funcionalidad
- `bugfix/TRAIN-XXX-descripcion` - Corrección de bugs
- `hotfix/TRAIN-XXX-descripcion` - Correcciones urgentes
- `docs/TRAIN-XXX-descripcion` - Documentación

### 3. Commits
```bash
git commit -m "TRAIN-XXX: Descripción clara del cambio"
```

Formato: `TRAIN-XXX: descripción`

### 4. Push y Pull Request
```bash
git push origin feature/TRAIN-XXX-descripcion
```

## Estándares de Código

### Java/Spring Boot
- Usar Google Java Style Guide
- Máximo 120 caracteres por línea
- Nombres de variables en camelCase
- Nombres de clases en PascalCase
- Usar lombok para getters/setters
- Documentar con JavaDoc métodos públicos

### Ejemplo:
```java
/**
 * Obtiene un entrenamiento por su ID.
 *
 * @param id ID del entrenamiento
 * @return WorkoutDTO
 * @throws ResourceNotFoundException si no existe
 */
@GetMapping("/{id}")
public ResponseEntity<WorkoutDTO> getWorkout(@PathVariable Long id) {
    return ResponseEntity.ok(workoutService.getWorkoutById(id));
}
```

## Testing

### Cobertura Mínima
- 80% de cobertura en métodos
- Todos los casos críticos testeados
- Tests de integración para APIs

### Estructura de Tests
```java
@DisplayName("WorkoutService")
class WorkoutServiceTest {
    
    @Nested
    @DisplayName("getWorkoutById")
    class GetWorkoutByIdTests {
        
        @Test
        @DisplayName("debe retornar workout cuando existe")
        void shouldReturnWorkout() {
            // Given
            // When
            // Then
        }
    }
}
```

## Pull Request

### Checklist antes de enviar
- [ ] Tests pasan localmente
- [ ] Cobertura >= 80%
- [ ] Código formateado correctamente
- [ ] Sin warnings de SonarCloud
- [ ] Documentación actualizada
- [ ] CHANGELOG actualizado

### Descripción del PR
Usar la plantilla provista en `.github/pull_request_template.md`

## Branch Protection Rules

1. ✅ Require pull request reviews before merging (1 approval)
2. ✅ Require status checks to pass before merging
   - Build must pass
   - Tests must pass
   - Code coverage >= 80%
   - SonarCloud quality gate must pass
3. ✅ Require branches to be up to date before merging
4. ✅ Require code reviews from code owners
5. ✅ Dismiss stale pull request approvals
6. ✅ Require signed commits

## Versionado
Seguimos [Semantic Versioning](https://semver.org/):
- MAJOR.MINOR.PATCH (ej: 1.2.3)
- MAJOR: cambios incompatibles
- MINOR: nueva funcionalidad compatible
- PATCH: correcciones de bugs

## Licencia
Al contribuir, aceptas que tu código será licenseado bajo MIT.
