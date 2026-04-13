# Guía de Contribución - TrAIn Backend

## Código de Conducta

Este proyecto adopta el Código de Conducta del Contribuyente. Al participar, se espera que mantengas este código. Por favor reporta comportamiento inaceptable a los mantenedores del proyecto.

## ¿Cómo Contribuir?

### Reportar Bugs

1. **Usa el título descriptivo** para el issue
2. **Describe el comportamiento exacto** que observaste y cuál era el comportamiento esperado
3. **Proporciona ejemplos específicos** para demostrar los pasos
4. **Describe el comportamiento observado** y por qué es un problema
5. **Proporciona el ambiente**: Java 21, Spring Boot 3, versión de dependencias, SO, etc.

### Sugerir Mejoras

1. Usa un título descriptivo
2. Proporciona una descripción clara de la mejora
3. Lista algunos ejemplos del comportamiento actual y el comportamiento esperado
4. Explica por qué esta mejora sería útil

### Pull Requests

1. **Fork y crea tu rama** desde `develop`:
   ```bash
   git checkout -b feature/TRAIN-XXX-descripcion
   ```

2. **Sigue el formato de commits**:
   ```
   [TRAIN-XXX] Título corto (50 caracteres máx)
   
   Descripción detallada del cambio. Explica qué y por qué,
   no cómo. Mantén líneas a 72 caracteres.
   
   Fixes #123
   ```

3. **Asegúrate de que el código cumple con los estándares**:
   ```bash
   mvn clean verify
   ```

4. **Añade tests** para cualquier nueva funcionalidad

5. **Actualiza la documentación**

6. **Push a tu fork** y **abre un Pull Request** en `develop`

## Estándares de Código

### Convenciones de Nombres

- **Clases**: `PascalCase` (ej: `UserController`)
- **Métodos/Variables**: `camelCase` (ej: `getUserById`)
- **Constantes**: `UPPER_SNAKE_CASE` (ej: `MAX_RETRY_COUNT`)
- **Paquetes**: `lowercase.separated.by.dots`

### Estructura de Clases

```java
public class UserController {
    // 1. Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    
    // 2. Inyecciones
    @Autowired
    private UserService userService;
    
    // 3. Constructores
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    // 4. Métodos públicos (order: GET, POST, PUT, DELETE)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        // ...
    }
    
    // 5. Métodos privados
    private void validateUser(User user) {
        // ...
    }
}
```

### Checklist antes de hacer Push

- [ ] He ejecutado `mvn clean verify`
- [ ] He añadido tests para nuevas funcionalidades
- [ ] He actualizado la documentación relevante
- [ ] Los commits están limpios y bien documentados
- [ ] No hay conflictos con la rama base
- [ ] El código está formateado según estándares
- [ ] No hay warnings sin resolver

## Proceso de Review

1. Al menos 2 approvals requeridos
2. Todos los checks (tests, linting) deben pasar
3. Debe estar actualizado con `develop`
4. No debe haber cambios solicitados pendientes

## Merge a Producción

- Solo desde `main` a través de release branches
- Requiere 3 approvals
- Todos los checks deben pasar
- Merge requiere squash

## Ayuda Adicional

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Guide](https://maven.apache.org/guides/)
- [Git Workflow](https://guides.github.com/introduction/flow/)

¡Gracias por contribuir! 🙌
