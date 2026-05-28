# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 의존성 캐싱을 위해 Gradle 파일 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# 소스 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# 런타임 이미지 (JRE만 포함 - 이미지 경량화)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S anonform && adduser -S anonform -G anonform
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown anonform:anonform app.jar
USER anonform

EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
