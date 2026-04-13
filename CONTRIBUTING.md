# Guía de Contribución - TrAIn Backend

## Código de Conducta

Este proyecto se adhiere al [Código de Conducta Contributor Covenant](CODE_OF_CONDUCT.md).

## ¿Cómo contribuir?

### Reportar bugs

1. Verifica si el bug ya ha sido reportado en [Issues](https://github.com/iagop03/train-backend/issues)
2. Si no existe, crea un nuevo issue con:
   - Título descriptivo
   - Descripción detallada
   - Pasos para reproducir
   - Comportamiento esperado vs actual
   - Screenshots si aplica

### Sugerir mejoras

1. Usa la etiqueta `enhancement` en los issues
2. Proporciona descripción clara del caso de uso
3. Lista alternativas consideradas

### Pull Requests

#### Setup

```bash
git clone https://github.com/iagop03/train-backend.git
cd train-backend
git checkout -b feature/TRAIN-XXX-descripcion
```

#### Commit

Seguir [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(auth): agregar autenticación OAuth2
fix(users): corregir validación de email
docs(api): actualizar documentación de endpoints
```

#### Antes de hacer Push

```bash
# Run tests
mvn test

# Run analysis
mvn clean verify

# Check formatting
mvn spotless:check
```

#### Enviar PR

1. Push a tu fork
2. Abre un PR contra `main` o `develop`
3. Usa la plantilla de PR
4. Completa todos los checkpoints
5. Espera revisión (2+ aprobaciones requeridas)

## Estándares de código

### Estilo Java

- Java 21 features
- Google Java Style Guide
- Máximo 120 caracteres por línea
- 4 espacios de indentación

### Naming

- Clases: `PascalCase`
- Métodos/variables: `camelCase`
- Constantes: `UPPER_SNAKE_CASE`
- Paquetes: `com.train.feature`

### Testing

- Mínimo 80% de cobertura
- Unit tests para lógica de negocio
- Integration tests para controllers
- Tests descriptivos

```java
@Test
void shouldReturnUserWhenIdExists() {
    // Given
    Long userId = 1L;
    User expected = new User(userId, "John");
    when(repository.findById(userId)).thenReturn(Optional.of(expected));

    // When
    User actual = service.getUserById(userId);

    // Then
    assertEquals(expected, actual);
}
```

## Proceso de revisión

1. **Automated checks**: CI pipeline debe pasar
2. **Code review**: 2+ approvals de maintainers
3. **Approval**: Squash & merge a main/develop

## Licencia

Al contribuir, aceptas que tu código será bajo licencia MIT.
