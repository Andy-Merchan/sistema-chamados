# ---------- Etapa 1: build (Maven + JDK 21) ----------
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

# Primeiro só o pom.xml: as dependências ficam em cache e não são
# baixadas de novo enquanto o pom.xml não mudar
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
# -DskipTests: o teste contextLoads precisa de um banco, que não existe durante o build
RUN mvn package -DskipTests -B

# ---------- Etapa 2: execução (apenas JRE 21) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
