# Multi-stage build
FROM eclipse-temurin:21-jdk as builder

WORKDIR /workspace

COPY . .

ARG PROFILE=dev

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -P ${PROFILE}

# Runtime stage
FROM eclipse-temurin:21-jre

RUN useradd -m -u 1000 train

WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar

RUN chown -R train:train /app

USER train

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]