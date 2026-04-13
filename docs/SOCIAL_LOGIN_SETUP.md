# Configuración de Social Login (Google y Apple)

## Descripción General

Esta documentación describe cómo configurar Google y Apple como Identity Providers (IdP) en Keycloak para TrAIn.

## Requisitos Previos

1. Keycloak instalado y ejecutándose
2. Cuentas de desarrollador en:
   - Google Cloud Console
   - Apple Developer Program

## Configuración de Google

### 1. Crear credenciales en Google Cloud Console

1. Acceder a [Google Cloud Console](https://console.cloud.google.com/)
2. Crear un nuevo proyecto o seleccionar uno existente
3. Ir a "APIs & Services" > "Credentials"
4. Crear "OAuth 2.0 Client ID"
5. Configurar consentimiento OAuth:
   - Tipo: External
   - Scopes: email, profile, openid
   - URIs de redirección autorizados:
     - `http://localhost:8080/auth/realms/train/broker/google/endpoint`
     - `https://your-production-domain/auth/realms/train/broker/google/endpoint`

6. Obtener:
   - Client ID
   - Client Secret

### 2. Configurar en Keycloak

1. Acceder a Keycloak Admin Console
2. Seleccionar realm "train"
3. Ir a "Identity Providers" > "Add Provider"
4. Seleccionar "Google"
5. Configurar:
   - Client ID: `{GOOGLE_CLIENT_ID}`
   - Client Secret: `{GOOGLE_CLIENT_SECRET}`
   - Trust Email: ON
   - Sync Mode: FORCE

6. Guardar

## Configuración de Apple

### 1. Crear credenciales en Apple Developer Program

1. Acceder a [Apple Developer Portal](https://developer.apple.com/)
2. Ir a "Certificates, Identifiers & Profiles"
3. Crear un "Service ID":
   - Identifier: `com.example.train`
   - Enable "Sign in with Apple"

4. Configurar "Sign in with Apple":
   - Primary Web Application Domain: `your-domain.com`
   - Return URLs:
     - `http://localhost:8080/auth/realms/train/broker/apple/endpoint`
     - `https://your-production-domain/auth/realms/train/broker/apple/endpoint`

5. Crear una "Private Key" para el Service ID
6. Obtener:
   - Team ID
   - Service ID (Client ID)
   - Key ID
   - Private Key (archivo .p8)

### 2. Configurar en Keycloak

1. Acceder a Keycloak Admin Console
2. Seleccionar realm "train"
3. Ir a "Identity Providers" > "Add Provider"
4. Seleccionar "Apple"
5. Configurar:
   - Team ID: `{APPLE_TEAM_ID}`
   - Key ID: `{APPLE_KEY_ID}`
   - Client ID: `{APPLE_CLIENT_ID}`
   - Private Key: (contenido del archivo .p8)
   - Trust Email: ON
   - Sync Mode: FORCE

6. Guardar

## Variables de Entorno

Configurar en `.env`:

```bash
# Google OAuth
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Apple OAuth
APPLE_CLIENT_ID=com.example.train
APPLE_TEAM_ID=your-apple-team-id
APPLE_KEY_ID=your-apple-key-id
APPLE_PRIVATE_KEY=your-apple-private-key
```

## Flujo de Autenticación

### Frontend (Web)

1. Usuario hace clic en "Continuar con Google" o "Continuar con Apple"
2. Se redirige a Keycloak con parámetros:
   - `client_id`: train-web
   - `response_type`: code
   - `scope`: openid profile email
   - `redirect_uri`: http://localhost:4200/auth/callback
   - `kc_idp_hint`: google o apple

3. Keycloak redirige al IdP (Google/Apple)
4. Usuario se autentica en el IdP
5. IdP redirige a Keycloak con código de autorización
6. Keycloak intercambia el código por un token JWT
7. Keycloak redirige al cliente con el token
8. Cliente almacena el token y accede a la aplicación

### Mobile (Flutter)

1. Usuario hace clic en "Continuar con Google" o "Continuar con Apple"
2. Se abre el flujo nativo de Google/Apple Sign-In
3. Usuario se autentica
4. Se obtiene un token del IdP
5. Se envía el token al backend
6. Backend valida el token y crea una sesión
7. Se devuelve el token JWT al cliente
8. Cliente almacena el token y accede a la aplicación

## Mapeo de Atributos

### Google

- `sub` → User ID
- `email` → Email
- `name` → Full Name
- `given_name` → First Name
- `family_name` → Last Name
- `picture` → Avatar

### Apple

- `sub` → User ID
- `email` → Email
- `name` → Full Name (solo en primer login)

## Testing

### Local

1. Iniciar Keycloak en puerto 8080
2. Iniciar backend en puerto 8081
3. Iniciar frontend en puerto 4200
4. Navegar a http://localhost:4200/login
5. Hacer clic en "Continuar con Google" o "Continuar con Apple"

### Endpoints de Prueba

```bash
# Obtener información del usuario actual
curl -H "Authorization: Bearer {token}" http://localhost:8081/api/v1/auth/me

# Logout
POST http://localhost:8081/api/v1/auth/logout
```

## Troubleshooting

### Error: "redirect_uri_mismatch"

- Verificar que el redirect_uri en la solicitud coincida exactamente con el configurado en Google/Apple
- Incluir protocolo (http/https)
- Sin trailing slash

### Error: "invalid_client"

- Verificar que Client ID y Client Secret sean correctos
- Verificar que las variables de entorno estén correctamente configuradas

### Token Inválido

- Verificar que Keycloak esté ejecutándose
- Verificar que el realm "train" exista
- Verificar que el cliente "train-api" esté creado en Keycloak

## Seguridad

1. Nunca exponer Client Secrets en código cliente
2. Usar HTTPS en producción
3. Configurar CORS correctamente
4. Validar siempre los tokens en el backend
5. Usar almacenamiento seguro para tokens en mobile

## Referencias

- [Keycloak Social Identity Providers](https://www.keycloak.org/docs/latest/server_admin/#social-identity-providers)
- [Google OAuth 2.0](https://developers.google.com/identity/protocols/oauth2)
- [Apple Sign in with Apple](https://developer.apple.com/sign-in-with-apple/)
