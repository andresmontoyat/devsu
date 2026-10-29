# syntax=docker/dockerfile:1

# ---- build: compiles the boot jar and splits it into layers ----
FROM eclipse-temurin:21.0.12.1_1-jdk-alpine AS build
WORKDIR /workspace
COPY . .
# Tests run in CI and through ./gradlew build; the image build only packages.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :bootstrap:bootJar --no-daemon --console=plain -x test
# One layer per change rate: dependencies rarely change, application classes on every commit.
RUN java -Djarmode=tools -jar bootstrap/build/libs/cliente-service.jar \
    extract --layers --launcher --destination /workspace/extracted

# ---- runtime: JRE only, non-root ----
FROM eclipse-temurin:21.0.12.1_1-jre-alpine
RUN addgroup -g 10001 app && adduser -D -H -u 10001 -G app app
WORKDIR /app
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./
USER 10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8081 9081
# Readiness on the management port: it already includes the database (and Valkey in cuenta).
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
  CMD wget -qO- http://127.0.0.1:9081/actuator/health/readiness || exit 1
# Exec form: java is PID 1 and receives SIGTERM, so the graceful shutdown drains in-flight requests.
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
