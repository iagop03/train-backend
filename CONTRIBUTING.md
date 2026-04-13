# Guía de Contribución - TrAIn Backend

## Comenzar

1. Fork el repositorio
2. Clona tu fork: `git clone https://github.com/tu-usuario/train-backend.git`
3. Añade el repositorio upstream: `git remote add upstream https://github.com/iagop03/train-backend.git`
4. Crea una rama para tu feature: `git checkout -b feature/TRAIN-XXX-descripcion`

## Requisitos

- Java 21+
- Maven 3.8+
- PostgreSQL 15+
- Docker (opcional, recomendado)

## Configuración Local

```bash
# Instalar dependencias
mvn clean install

# Ejecutar tests
mvn test

# Ejecutar la aplicación
mvn spring-boot:run
```

## Estándares de Código

- Sigue el Google Java Style Guide
- Usa nombres descriptivos en inglés
- Comenta código complejo
- Escribe tests para nuevas funcionalidades
- Mantén cobertura de código > 80%

## Convenciones de Commits

```
[TRAIN-XXX] Tipo: Descripción breve

Descripción detallada si es necesario.

Tipos permitidos:
- feat: Nueva funcionalidad
- fix: Corrección de bug
- refactor: Refactorización de código
- test: Añadir o actualizar tests
- docs: Cambios en documentación
- style: Cambios de formato
- chore: Cambios en dependencias o configuración
```

## Pull Request Process

1. Asegúrate de que tu rama está actualizada: `git pull upstream develop`
2. Ejecuta tests localmente: `mvn test`
3. Push a tu fork y crea un PR contra `develop`
4. Llena el PR template completamente
5. Espera a que los CI checks pasen
6. Solicita revisión de al menos 1 maintainer
7. Responde a comentarios de revisión
8. Merge solo después de aprobación

## Reportar Bugs

Abre un issue con:
- Descripción clara del bug
- Pasos para reproducir
- Comportamiento esperado vs actual
- Stack trace si aplica
- Versión Java y SO

## Sugerir Mejoras

Abre un issue con:
- Descripción clara de la mejora
- Caso de uso
- Beneficios
- Posibles alternativas

## Preguntas

Para preguntas, abre una discussion o contacta a @iagop03
