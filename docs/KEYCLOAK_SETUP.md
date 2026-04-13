# Keycloak Setup Guide for TrAIn Project

## Overview
This document describes how to configure Keycloak for the TrAIn project with JWT token validation and role-based access control.

## Prerequisites
- Keycloak server running (version 23.0.0 or higher)
- Spring Boot backend configured
- PostgreSQL or MongoDB for user data storage

## 1. Keycloak Realm Configuration

### Create Realm
1. Login to Keycloak Admin Console
2. Create a new realm named `train`
3. Configure realm settings:
   - Enable "User-Managed Access"
   - Set token expiration: 1 hour
   - Set refresh token expiration: 7 days

### Define Roles
Create the following roles in the realm:
- `admin` - Full system access
- `trainer` - Create and manage workouts
- `user` - View and track workouts

## 2. Create OAuth2 Client

### Client Configuration
1. Go to Clients section
2. Create new client: `train-backend`
3. Configure:
   - Client Type: OpenID Connect
   - Client Authentication: Enabled
   - Generate Secret: Yes

### Access Type Configuration
1. Set Access Type: `Confidential`
2. Enable Service Accounts: Yes
3. Enable OAuth 2.0 Device Authorization Grant: Yes

### Valid Redirect URIs
```
http://localhost:8081/*
http://localhost:4200/*
https://train.app.com/*
```

### Client Roles
Add roles to the client:
- admin
- trainer
- user

### Mappers Configuration

Create a scope mapper for roles:
1. Go to Client Scopes
2. Create new scope: `roles`
3. Add Mapper: "User Client Role"
   - Token Claim Name: `roles`
   - Add to access token: Yes

## 3. JWT Token Claims Configuration

### Default Client Scopes
Assign these scopes to the client:
- openid
- profile
- email
- roles

### Token Mappers
Create mappers to add custom claims:

1. **Realm Roles Mapper**
   - Mapper Type: User Realm Role
   - Token Claim Name: `realm_access.roles`
   - Add to access token: Yes

2. **Client Roles Mapper**
   - Mapper Type: User Client Role
   - Token Claim Name: `resource_access.train-backend.roles`
   - Add to access token: Yes

3. **Email Mapper**
   - Mapper Type: User Property
   - Property: email
   - Token Claim Name: email
   - Add to access token: Yes

## 4. Spring Boot Configuration

### Environment Variables
```bash
KEYCLOAK_REALM=train
KEYCLOAK_AUTH_SERVER_URL=http://localhost:8080
KEYCLOAK_CLIENT_ID=train-backend
KEYCLOAK_CLIENT_SECRET=<generated-secret>
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/train
KEYCLOAK_JWK_SET_URI=http://localhost:8080/realms/train/protocol/openid-connect/certs
```

### application.yml Configuration
See `src/main/resources/application.yml` for complete configuration.

## 5. Role-Based Access Control Setup

### Security Rules
Implemented in `SecurityConfig.java`:

**Public Endpoints**
- `/public/**` - No authentication required
- `/actuator/health` - Health checks
- `/auth/login` - Login endpoint
- `/auth/logout` - Logout endpoint

**Trainer Endpoints** (Requires TRAINER role)
- `GET /api/workouts/**` - View workouts
- `POST /api/workouts` - Create workout
- `PUT /api/workouts/**` - Update workout
- `DELETE /api/workouts/**` - Delete workout

**User Endpoints** (Requires USER or TRAINER role)
- `GET /api/user/profile` - View profile
- `PUT /api/user/profile` - Update profile
- `GET /api/user/workouts` - View workouts

**Admin Endpoints** (Requires ADMIN role)
- `/api/admin/**` - All admin operations

## 6. Testing JWT Token

### Get Access Token
```bash
curl -X POST http://localhost:8080/realms/train/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=train-backend" \
  -d "client_secret=<secret>" \
  -d "username=testuser" \
  -d "password=password" \
  -d "grant_type=password"
```

### Use Token in Request
```bash
curl -X GET http://localhost:8081/api/user/profile \
  -H "Authorization: Bearer <access_token>"
```

### Decode JWT
Use https://jwt.io to decode and verify token structure.

## 7. User Management

### Create Test Users
1. Go to Users section in Keycloak Admin Console
2. Create users:
   - `trainer_user` - Assign TRAINER role
   - `regular_user` - Assign USER role
   - `admin_user` - Assign ADMIN role
3. Set password and temporary flag

### Assign Roles to Users
1. Select user
2. Go to Role Mappings
3. Assign realm roles
4. Assign client roles

## 8. Security Best Practices

### Token Validation
- Tokens are validated against JWK Set from Keycloak
- Token signature is verified using public keys
- Token expiration is checked automatically
- Issuer URI must match configuration

### Password Policy
Configure in Keycloak:
- Minimum length: 12 characters
- Require uppercase letters
- Require lowercase letters
- Require numbers
- Require special characters

### SSL/TLS
- Set `ssl-required: all` in production
- Configure HTTPS for all endpoints
- Use valid SSL certificates

### Token Expiration
- Access token: 1 hour (configurable)
- Refresh token: 7 days (configurable)
- Use refresh token flow for long-lived sessions

## 9. Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Verify token is included in Authorization header
   - Check token expiration
   - Verify issuer URI matches configuration

2. **403 Forbidden**
   - Check user has required roles
   - Verify role claim is in JWT token
   - Check RBAC configuration

3. **Invalid Signature**
   - Verify JWK Set URI is correct
   - Check Keycloak is running
   - Verify client secret is correct

### Debug Logging
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.oauth2: DEBUG
```

## 10. Production Deployment

### Checklist
- [ ] Use HTTPS/TLS for all endpoints
- [ ] Configure SSL verification (`ssl-required: all`)
- [ ] Use strong, randomly generated client secrets
- [ ] Enable CSRF protection
- [ ] Configure CORS properly
- [ ] Set up log aggregation
- [ ] Enable rate limiting
- [ ] Configure backup and disaster recovery
- [ ] Set up monitoring and alerting
- [ ] Regular security audits

## References
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2 Documentation](https://spring.io/projects/spring-security-oauth2-resource-server)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
