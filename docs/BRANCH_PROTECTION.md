# Configuración de Branch Protection - TrAIn Backend

## Descripción
Este documento describe las reglas de protección de ramas configuradas en el repositorio.

## Ramas Protegidas

### main
- Rama de producción
- Requiere revisión de PR (1 approval mínimo)
- Requiere que todos los checks pasen
- Requiere commits firmados
- Requiere que la rama esté actualizada

### develop
- Rama de desarrollo
- Requiere revisión de PR (1 approval)
- Requiere que todos los checks pasen
- Requiere que la rama esté actualizada

## Checks Requeridos

### 1. Build
```
Backend CI/CD → build-and-test
```
Verifica que el proyecto compila correctamente con Maven.

### 2. Tests
```
Backend CI/CD → build-and-test (test job)
```
Ejecuta todos los tests unitarios e integración.
Cobertura mínima requerida: 80%

### 3. Code Quality
```
Backend CI/CD → build-and-test (SonarCloud)
```
Verifica que el código cumple con los estándares de calidad.
Quality Gate debe pasar.

### 4. Security
```
Backend CI/CD → security-scan
```
Escanea vulnerabilidades con Snyk.

## Cómo Hacer Merge

### Flujo estándar:
1. Crear rama desde `develop`
2. Hacer commit con mensaje format `TRAIN-XXX: descripción`
3. Hacer push y crear PR
4. Esperar a que todos los checks pasen
5. Solicitar review (mínimo 1 approval)
6. Hacer merge a `develop`
7. Una vez testeado en develop, hacer PR a `main`
8. Hacer merge a `main`

## Bypass de Branch Protection

Solo administradores del repositorio pueden hacer bypass.
Esto debe ser excepcional y documentado.

## Review Process

### Qué revisar:
1. ✅ Código sigue convenciones
2. ✅ Tests están presentes y pasan
3. ✅ No hay degradación de cobertura
4. ✅ Documentación está actualizada
5. ✅ No hay conflictos de merge
6. ✅ Commits tienen mensaje claro

### Feedback:
- Comment: Comentario que no bloquea merge
- Request Changes: Requiere modificaciones
- Approve: Aprueba el PR

## Monitoreo

### Dashboard:
Ver estado de checks en: `https://github.com/iagop03/train-backend/actions`

### Alertas:
- Branches con checks fallidos
- PRs sin review después de 24h
- Covertura por debajo de 80%

## FAQ

### P: ¿Puedo hacer merge sin que pasen los tests?
R: No, los tests deben pasar siempre.

### P: ¿Cuándo se usa develop vs main?
R: develop para cambios en desarrollo, main solo para releases.

### P: ¿Qué pasa si necesito urgentemente hacer un hotfix?
R: Crear rama `hotfix/TRAIN-XXX` desde main, hacer PR a main con urgencia.
