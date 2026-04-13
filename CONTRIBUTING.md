# Contributing to TrAIn Backend

## Código de Conducta

Este proyecto adhiere a un Código de Conducta que esperamos que todos los contribuyentes respeten.

## Cómo Contribuir

### Reportando Bugs

1. Usa la etiqueta "bug" en el issue
2. Describe el comportamiento observado vs esperado
3. Incluye pasos para reproducir
4. Adjunta capturas de pantalla si es posible
5. Especifica tu entorno (Java version, OS, etc)

### Sugerencias de Features

1. Usa la etiqueta "enhancement"
2. Describe la feature y su caso de uso
3. Explica por qué crees que es útil
4. Lista ejemplos de cómo se usaría

### Pull Requests

1. Fork el repositorio
2. Crea una rama: `git checkout -b feature/TRAIN-XXX`
3. Commit con mensajes descriptivos
4. Sigue las convenciones de código
5. Añade tests para nuevas features
6. Push a tu fork
7. Abre un PR con descripción detallada

## Convenciones de Código

### Java/Spring Boot

```java
// Usar nomenclatura clara y significativa
private UserRepository userRepository;

// Métodos pequeños y focused
@Override
public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
}

// Usar Optional
optional.ifPresentOrElse(
    user -> log.info("User found: {}", user),
    () -> log.warn("User not found")
);
```

### Commits

```
feat: agregar nueva feature
fix: corregir bug
docs: actualizar documentación
style: cambios de formato
refactor: refactorización sin cambio de feature
test: agregar tests
chore: tareas de mantenimiento
```

### Tests

- Mínimo 80% de cobertura
- Usar JUnit 5 y Mockito
- Nombrar tests: `testShouldReturnUserWhenIdIsValid`
- Usar AAA pattern: Arrange, Act, Assert

## Proceso de Review

1. Mínimo 2 aprobaciones de code owners
2. Todos los checks deben pasar
3. Documentación debe estar actualizada
4. Tests deben cubrir los cambios

## Versionado

Seguimos Semantic Versioning:
- MAJOR: cambios incompatibles
- MINOR: nuevas features
- PATCH: bug fixes