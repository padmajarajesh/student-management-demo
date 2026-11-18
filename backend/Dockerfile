# ---------------------------------------------------------------------
# Render runs Java apps through Docker. This file performs exactly the
# Render build/start steps:
#   build: ./mvnw clean package -DskipTests
#   start: java -jar target/student-management.jar
# ---------------------------------------------------------------------

# ---- Stage 1: build the jar ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -B clean package -DskipTests

# ---- Stage 2: small runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 1001 appuser
COPY --from=build /app/target/student-management.jar app.jar
USER appuser

ENV SPRING_PROFILES_ACTIVE=prod
# Keep the heap inside the container's memory limit (Render free tier = 512 MB)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Render sets PORT; the app reads it via server.port=${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
