#!/bin/bash
echo "Iniciando build para debug..."

# Dar permissão ao Maven Wrapper
chmod +x ./mvnw

# Limpar e fazer o build
echo "Executando Maven build..."
./mvnw clean package -DskipTests

# Verificar se o JAR foi gerado
echo "Verificando arquivos JAR gerados:"
find ./target -name "*.jar" -type f

# Mostrar tamanho e data de criação
echo "Detalhes dos arquivos JAR:"
ls -la ./target/*.jar

echo "Build concluído." 