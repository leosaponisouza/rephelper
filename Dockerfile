FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace/app

# Copiar apenas os arquivos necessários para resolver dependências primeiro
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permissão de execução ao mvnw e baixar dependências
RUN chmod +x ./mvnw && \
    ./mvnw dependency:go-offline -B

# Agora copiar o código-fonte
COPY src src

# Construir a aplicação
RUN ./mvnw package -DskipTests

# Criar imagem final
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Adicionar usuário não-root
RUN addgroup --system --gid 1001 javaapp && \
    adduser --system --uid 1001 --ingroup javaapp javaapp && \
    apk add --no-cache wget curl

# Copiar o JAR e o script de inicialização
COPY --from=build /workspace/app/target/*.jar app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# Configurar permissões
RUN chmod +x /app/docker-entrypoint.sh && \
    chown -R javaapp:javaapp /app

# Configurações do usuário e executável
USER javaapp
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://localhost:8080/debug/info || exit 1

# Comando para iniciar a aplicação
ENTRYPOINT ["/app/docker-entrypoint.sh"] 