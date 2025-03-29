#!/bin/bash

# Script para construir a aplicação para produção

echo "Iniciando build para produção..."

# Definir variáveis de ambiente para produção
export SPRING_PROFILES_ACTIVE=prod

# Limpar artefatos anteriores
echo "Limpando builds anteriores..."
./mvnw clean

# Executar testes
echo "Executando testes..."
./mvnw test

# Se os testes passarem, fazer o build para produção
if [ $? -eq 0 ]; then
    echo "Testes passaram com sucesso. Gerando build para produção..."
    ./mvnw package -DskipTests -Pprod
    
    echo "Build finalizado com sucesso! O arquivo JAR está em: target/rephelper-0.0.1-SNAPSHOT.jar"
    echo "Execute com: java -jar -Dspring.profiles.active=prod target/rephelper-0.0.1-SNAPSHOT.jar"
else
    echo "Os testes falharam. Corrigindo erros antes de gerar o build de produção."
    exit 1
fi 