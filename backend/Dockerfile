# 构建阶段
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle shadowJar --no-daemon

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# 复制构建产物
COPY --from=build /app/build/libs/*-all.jar app.jar

# 环境变量
ENV PORT=8080
ENV JWT_SECRET=change-this-in-production
ENV JWT_ISSUER=medledger
ENV JWT_AUDIENCE=medledger-users
ENV DATABASE_URL=jdbc:postgresql://localhost:5432/medledger
ENV DATABASE_USER=medledger
ENV DATABASE_PASSWORD=password

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
