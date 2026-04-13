# Testcontainers Integration Tests - TrAIn Backend

## Overview

This document describes the comprehensive test suite for TrAIn backend using Testcontainers. The test suite provides integration testing for both PostgreSQL and MongoDB databases in an isolated, reproducible environment.

## Architecture

### Key Components

1. **TestcontainersConfiguration** - Central configuration for container management
2. **Test Database Containers**:
   - PostgreSQL 15 Alpine for relational data
   - MongoDB 7.0 for document storage
3. **Repository Integration Tests** - CRUD operations for each repository
4. **Cross-Database Integration Tests** - Multi-database workflow validation

## Test Coverage

### PostgreSQL Tests

#### UserRepositoryIntegrationTest
- Save user successfully
- Find user by email
- Find user by username
- Update user
- Delete user
- Check email existence

#### ExerciseRepositoryIntegrationTest
- Save exercise to database
- Find exercises by user ID
- Find exercises by category
- Find exercise by ID
- Update exercise
- Delete exercise
- Count exercises by user

### MongoDB Tests

#### WorkoutSessionRepositoryIntegrationTest
- Save workout session
- Find sessions by user ID
- Find completed sessions
- Find session by ID
- Update session
- Delete session
- Find sessions by date range
- Count sessions by user
- MongoDB collection cleanup

#### WorkoutPlanRepositoryIntegrationTest
- Save workout plan
- Find plans by user ID
- Find active plans
- Find plans by goal
- Find plan by ID
- Update plan
- Delete plan
- Find plans by difficulty
- Find user's active plans

#### PerformanceMetricsRepositoryIntegrationTest
- Save performance metrics
- Find metrics by user ID
- Find metrics by exercise name
- Find metrics by user and exercise
- Find metrics by date range
- Find metrics by ID
- Update metrics
- Delete metrics
- Find max weight for exercise

### Cross-Database Tests

#### CrossDatabaseIntegrationTest
- User creation in PostgreSQL + session in MongoDB
- Exercise (PostgreSQL) and session (MongoDB) linking
- Complete workout lifecycle across databases
- Concurrent operations on both databases
- Referential integrity validation

## Running Tests

### Prerequisites

- Docker installed and running
- Java 21+
- Maven 3.6+

### Run All Integration Tests

```bash
mvn clean test
```

### Run Specific Test Class

```bash
mvn test -Dtest=UserRepositoryIntegrationTest
mvn test -Dtest=WorkoutSessionRepositoryIntegrationTest
mvn test -Dtest=CrossDatabaseIntegrationTest
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report
```

### Run Integration Tests Only

```bash
mvn test -Dgroups=integration
```

## Configuration

### application-test.yml

The test profile configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/train_test
    username: trainuser
    password: trainpass
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/train_test
```

### Testcontainers Properties

- **PostgreSQL Container**:
  - Image: `postgres:15-alpine`
  - Database: `train_test`
  - User: `trainuser`
  - Password: `trainpass`
  - Reuse: Enabled (faster test execution)

- **MongoDB Container**:
  - Image: `mongo:7.0`
  - Reuse: Enabled

## Best Practices

### 1. Test Isolation

```java
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
```
Ensures clean state between tests by resetting the application context.

### 2. Resource Management

Containers are reused across test runs:

```java
container.withReuse(true);
```

### 3. Test Naming

Use descriptive names with `@DisplayName`:

```java
@DisplayName("Should save user to PostgreSQL successfully")
void testSaveUser() { }
```

### 4. Assertions

Use AssertJ for fluent assertions:

```java
assertThat(user).isNotNull();
assertThat(users).hasSize(2);
assertThat(users).extracting(User::getEmail)
    .containsExactlyInAnyOrder("test1@example.com", "test2@example.com");
```

## Troubleshooting

### Container Port Already in Use

Testcontainers automatically handles port mapping. If issues occur:

```bash
# Stop all containers
docker-compose down

# Clean Docker
docker system prune -f
```

### MongoDB Connection Issues

Ensure MongoDB version compatibility:

```java
DockerImageName.parse("mongo:7.0")
```

### Test Timeout

Increase timeout in test:

```java
@Test(timeout = 5000)
void slowTest() { }
```

## Performance Metrics

- **First Test Run**: ~30-45 seconds (container startup)
- **Subsequent Runs**: ~5-10 seconds (with container reuse)
- **Test Execution Time**: ~100-150ms per test

## CI/CD Integration

### GitHub Actions

```yaml
name: Integration Tests
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    services:
      docker:
        image: docker:20.10
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - run: mvn clean test
```

## Dependencies

- `org.testcontainers:testcontainers:1.19.7`
- `org.testcontainers:postgresql:1.19.7`
- `org.testcontainers:mongodb:1.19.7`
- `org.testcontainers:junit-jupiter:1.19.7`
- `org.springframework.boot:spring-boot-starter-test`
- `org.springframework.security:spring-security-test`

## Future Enhancements

1. Add Keycloak container for authentication tests
2. Implement test data factories
3. Add performance benchmarking
4. Implement API integration tests
5. Add test report generation

## References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [AssertJ Documentation](https://assertj.github.io/assertj-core-features-highlight.html)
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
