# ---------- 1) Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and download dependencies (cacheable layer)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Now copy source code and build
COPY src ./src
RUN mvn package -DskipTests -q


# ---------- 2) Extract Spring Boot layers ----------
FROM eclipse-temurin:21-jdk-alpine AS layertools
WORKDIR /layers

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Extract Spring Boot jar layers
# RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extractedxtracted
RUN java -Djarmode=layertools -jar app.jar extract


# ---------- 3) Runtime image ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy layers in order (least to most frequently changing)
COPY --from=layertools /layers/dependencies/ ./
COPY --from=layertools /layers/snapshot-dependencies/ ./
COPY --from=layertools /layers/spring-boot-loader/ ./
COPY --from=layertools /layers/application/ ./

EXPOSE 8081
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]
