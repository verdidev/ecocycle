# syntax=docker/dockerfile:1
# =============================================================================
# EcoCycle API - build multi-stage (Java 21)
#   Stage 1 (build):   Maven + JDK 21 compila o codigo-fonte e gera o JAR
#   Stage 2 (runtime): apenas JRE 21, usuario nao-root e healthcheck
# Nao depende de JAR previamente compilado na maquina.
# =============================================================================

# ---------- Stage 1: build ----------
# --platform=$BUILDPLATFORM: o JAR e independente de arquitetura, entao o Maven
# roda sempre na arquitetura nativa do builder (rapido mesmo em build multi-arch).
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1) Dependencias primeiro (camada cacheada enquanto o pom.xml nao mudar)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline || true

# 2) Codigo-fonte e empacotamento
#    Os testes rodam no job de CI antes do build da imagem, por isso -DskipTests aqui.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package \
    && cp target/ecocycle-*.jar /workspace/app.jar

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre

ARG APP_VERSION=dev
LABEL org.opencontainers.image.title="ecocycle-api" \
      org.opencontainers.image.description="EcoCycle API - Spring Boot 4 / Java 21 / Oracle" \
      org.opencontainers.image.version="${APP_VERSION}"

# curl e usado somente pelo HEALTHCHECK (versao nao fixada de proposito: segue os patches da distro)
# hadolint ignore=DL3008
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --no-create-home --shell /usr/sbin/nologin spring

WORKDIR /app
COPY --from=build --chown=spring:spring /workspace/app.jar app.jar

USER spring

ENV SERVER_PORT=8080 \
    APP_VERSION=${APP_VERSION} \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

# start-period generoso: a aplicacao so fica pronta depois do Flyway rodar no Oracle
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -fsS "http://localhost:${SERVER_PORT}/actuator/health" || exit 1

# Forma "exec" via sh para permitir JAVA_OPTS vindo do ambiente; exec garante que a
# JVM seja o PID 1 e receba o SIGTERM do "docker stop" (desligamento gracioso).
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
