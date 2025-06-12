FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

# --- RUN STAGE ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o JAR gerado na fase de build
COPY --from=builder /app/target/*.jar app.jar

# Expose a porta que o Spring Boot usa (padrão 8080)
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]