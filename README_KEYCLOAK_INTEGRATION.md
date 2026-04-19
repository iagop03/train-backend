# Keycloak Integration - Resource Server Configuration

## Overview

Train Backend is configured as an OAuth2 Resource Server using Spring Security 3. It validates JWT tokens issued by Keycloak without performing authentication itself.

## Architecture

### Flow

```
┌────────────────────────────────────────────────────────────────┐
│                     Client (Angular/Flutter)                   │
└────────────────────────────────────────────────────────────────┘
                          │
                    1. Login Request
                          │
                          ▼
┌────────────────────────────────────────────────────────────────┐
│                        Keycloak                                │
│              (Authentication & Authorization)                  │
└────────────────────────────────────────────────────────────────┘
                          │
              2. JWT Token (Bearer)
                          │
                          ▼
┌────────────────────────────────────────────────────────────────┐
│                    Train Backend API                           │
│         (Validates JWT against JWKS endpoint)                 │
└────────────────────────────────────────────────────────────────┘
```

## Configuration Files

### 1. Maven Dependencies (pom.xml)

**Key Dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

### 2. Application Configuration (application.yml)

**Development:**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/train
          jwk-set-uri: http://localhost:8080/realms/train/protocol/openid-connect/certs
```

**Production (application-prod.yml):**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://{KEYCLOAK_DOMAIN}/realms/train
          jwk-set-uri: https://{KEYCLOAK_DOMAIN}/realms/train/protocol/openid-connect/certs
```

### 3. Security Configuration (SecurityConfig.java)

**Features:**
- JWT token validation via JWKS endpoint
- Role-based access control (RBAC)
- CORS configuration for web and mobile clients
- Stateless session management
- Custom error handling for 401/403 responses

**Key Methods:**

```java
// Main security filter chain
@Bean
public SecurityFilterChain filterChain(HttpSecurity http)

// JWT Authentication converter
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter()

// CORS configuration
@Bean
public CorsConfigurationSource corsConfigurationSource()
```

### 4. JWT Utilities (JwtUtils.java)

**Static Methods for Easy Access in Controllers:**

```java
// Extract user UUID
UUID userId = JwtUtils.extractUserIdFromJwt();

// Extract all roles
Set<String> roles = JwtUtils.extractRolesFromJwt();

// Extract specific claim
Object claim = JwtUtils.extractClaimFromJwt("custom_claim");

// Get username
String username = JwtUtils.extractUsernameFromJwt();

// Get email
String email = JwtUtils.extractEmailFromJwt();

// Check if user has role
boolean isAdmin = JwtUtils.hasRole("admin");

// Verify JWT presence
boolean hasJwt = JwtUtils.isJwtPresent();

// Get raw token
String token = JwtUtils.getTokenString();
```

## Usage Examples

### Example 1: Protected Endpoint with Role Check

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile() {
        UUID userId = JwtUtils.extractUserIdFromJwt();
        String username = JwtUtils.extractUsernameFromJwt();
        
        return ResponseEntity.ok(new ProfileDto(userId, username));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        // Only ADMIN role can delete users
        return ResponseEntity.ok("User deleted");
    }
}
```

### Example 2: Using Custom Annotations

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok("Admin dashboard");
    }
}
```

### Example 3: Service Layer Security

```java
@Service
public class UserService {

    @Transactional
    public UserDto getCurrentUser() {
        UUID userId = JwtUtils.extractUserIdFromJwt();
        String email = JwtUtils.extractEmailFromJwt();
        Set<String> roles = JwtUtils.extractRolesFromJwt();
        
        return userRepository.findById(userId)
            .map(user -> new UserDto(user.getId(), user.getUsername(), email, roles))
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
```

## Security Endpoints

### Public Endpoints (No Authentication Required)

```
GET  /api/health/ping
GET  /api/health/status
GET  /actuator/**
```

### Protected Endpoints (JWT Required)

All other endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <JWT_TOKEN>
```

## JWT Token Structure

**Expected Claims in Keycloak JWT:**

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "preferred_username": "john.doe",
  "email": "john@example.com",
  "email_verified": true,
  "realm_access": {
    "roles": ["user", "trainer"]
  },
  "resource_access": {
    "train-web": {
      "roles": ["view-profile", "edit-workouts"]
    }
  },
  "iat": 1700000000,
  "exp": 1700003600,
  "iss": "http://localhost:8080/realms/train",
  "aud": "account",
  "typ": "Bearer"
}
```

## CORS Configuration

**Allowed Origins:**
- http://localhost:4200 (Angular dev)
- http://localhost:4300 (Flutter dev)
- https://train-web.example.com (Production web)
- https://api.train.example.com (Production API)

**Allowed Methods:** GET, POST, PUT, DELETE, PATCH, OPTIONS

**Exposed Headers:** Authorization, Content-Type

## Error Handling

### 401 Unauthorized

```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token"
}
```

### 403 Forbidden

```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

## Testing

### Run Security Tests

```bash
mvn test -Dtest=SecurityConfigTest
mvn test -Dtest=JwtUtilsTest
```

### Test with cURL

```bash
# Get health status (no auth required)
curl -X GET http://localhost:8081/api/health/ping

# Access protected endpoint without token (should fail)
curl -X GET http://localhost:8081/api/users/profile

# Access protected endpoint with valid token
curl -X GET http://localhost:8081/api/users/profile \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## Keycloak Setup

### 1. Create Realm

- Name: `train`
- Enabled: Yes

### 2. Create Client

- Client ID: `train-backend`
- Client Protocol: `openid-connect`
- Access Type: `confidential`
- Valid Redirect URIs: `http://localhost:8081/*`

### 3. Configure Roles

```
Realm Roles:
- admin
- user
- trainer
- moderator
```

### 4. Create Test User

- Username: `testuser`
- Email: `testuser@example.com`
- Assign role: `user`

## Troubleshooting

### Issue: "Cannot deserialize instance of JWT"

**Solution:** Ensure `issuer-uri` matches your Keycloak realm URL

### Issue: "Invalid signature" errors

**Solution:** Verify JWKS endpoint is accessible and Keycloak signing key is correct

### Issue: Token expires quickly

**Solution:** Adjust token lifetime in Keycloak realm settings > Tokens > Access Token Lifespan

### Issue: CORS errors

**Solution:** Verify client origin is in `corsConfigurationSource()` allowed origins list

## Environment Variables

**Production Deployment:**

```bash
export KEYCLOAK_DOMAIN="keycloak.example.com"
export SPRING_PROFILES_ACTIVE="prod"
export DB_HOST="cloudsql-host"
export DB_USER="db_user"
export DB_PASSWORD="db_password"
export MONGO_USER="mongo_user"
export MONGO_PASSWORD="mongo_password"
export MONGO_HOST="mongodb-atlas-host"
```

## References

- [Spring Security OAuth2 Resource Server](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [JWT.io - JWT Debugger](https://jwt.io)
- [Spring Security Method Security](https://spring.io/guides/topical/spring-security-architecture/)
