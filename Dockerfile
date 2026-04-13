# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /app/target/*.jar app.jar
USER spring:spring
EXPOSE 8080
ENV JAVA_OPTS=""
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD sh -c 'wget -qO- "http://127.0.0.1:${PORT:-8080}/actuator/health" || exit 1'
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
