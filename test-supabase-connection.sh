#!/bin/bash

echo "===== Teste de Conexão MySQL no Railway ====="

# Configurações do banco de dados com variáveis do Railway
MYSQL_HOST=${MYSQLHOST:-localhost}
MYSQL_PORT=${MYSQLPORT:-3306}
MYSQL_DATABASE=${MYSQLDATABASE:-rephelper}
MYSQL_USER=${MYSQLUSER:-root}
MYSQL_PASSWORD=${MYSQLPASSWORD:-password}

# Construir a URL JDBC
JDBC_URL="jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=true&serverTimezone=UTC"

echo "Verificando conexão com MySQL no Railway..."
echo "Host: $MYSQL_HOST"
echo "Porta: $MYSQL_PORT"
echo "Banco: $MYSQL_DATABASE"
echo "URL JDBC: $JDBC_URL"

# Instalar cliente MySQL se necessário
if ! command -v mysql &> /dev/null; then
    echo "Cliente MySQL não encontrado. Instalando..."
    apk add --no-cache mysql-client
fi

# Testar conexão MySQL
echo -n "Testando conexão MySQL... "
if mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1; then
    echo "SUCESSO"
else
    echo "FALHA"
    echo "Erro ao conectar ao banco de dados MySQL."
fi

# Verificar resolução DNS
echo -n "Verificando resolução DNS para $MYSQL_HOST... "
if nslookup "$MYSQL_HOST" > /dev/null 2>&1; then
    echo "SUCESSO"
else
    echo "FALHA"
    echo "Não foi possível resolver o endereço do host."
fi

# Verificar conectividade na porta
echo -n "Verificando conectividade com $MYSQL_HOST:$MYSQL_PORT... "
if nc -z -w5 "$MYSQL_HOST" "$MYSQL_PORT" > /dev/null 2>&1; then
    echo "SUCESSO"
else
    echo "FALHA"
    echo "Não foi possível estabelecer conexão com a porta."
fi

echo "===== Teste de Conexão Concluído =====" 