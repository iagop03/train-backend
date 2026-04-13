# Contribuir a TrAIn Backend

## Requisitos
- Java 21 o superior
- Maven 3.8.1+
- PostgreSQL 15+
- Git

## Configuración del Entorno

```bash
git clone https://github.com/iagop03/train-backend.git
cd train-backend
mvn clean install
```

## Ramas
- `main`: Producción (protegida)
- `develop`: Desarrollo (protegida)
- `feature/*`: Nuevas features
- `bugfix/*`: Correcciones de bugs
- `hotfix/*`: Hotfixes para producción

## Workflow de Git

1. Crear rama desde `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/TRAIN-XXX-descripcion
   ```

2. Hacer commits con mensaje descriptivo:
   ```bash
   git commit -m "TRAIN-XXX: descripción del cambio"
   ```

3. Push y crear Pull Request:
   ```bash
   git push origin feature/TRAIN-XXX-descripcion
   ```

## Estándares de Código
- Usar Google Java Style Guide
- Documentar métodos públicos con Javadoc
- Tests unitarios con cobertura mínima de 80%
- Spring Boot best practices

## Convenciones de Commits
```
[TRAIN-123] Tipo: descripción breve

Descripción detallada del cambio

Fixes #123
```

Tipos válidos: feat, fix, docs, style, refactor, test, chore

## Pull Request
- Usar el template de PR
- Mínimo 1 aprobación requerida
- CI/CD debe pasar
- Sin conflictos de merge
