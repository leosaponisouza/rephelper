#!/bin/bash
echo "Testando conexão com o banco de dados Supabase..."

# Configurações do banco de dados (substituir com seus valores reais)
DB_HOST="${DB_HOST:-seu-host-supabase.supabase.co}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-postgres}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-sua-senha}"
JDBC_DATABASE_URL="${JDBC_DATABASE_URL:-jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME}"

echo "Tentando conectar ao banco: $DB_HOST:$DB_PORT/$DB_NAME com usuário: $DB_USER"
echo "URL de conexão JDBC: $JDBC_DATABASE_URL"

# Instalar cliente PostgreSQL se necessário
if ! command -v psql &> /dev/null; then
    echo "Cliente PostgreSQL não encontrado. Instalando..."
    sudo apt-get update
    sudo apt-get install -y postgresql-client
fi

# Testar conexão com o banco
export PGPASSWORD="$DB_PASSWORD"
if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1" > /dev/null 2>&1; then
    echo "✅ Conexão com o banco de dados Supabase bem-sucedida!"
else
    echo "❌ Falha ao conectar ao banco de dados Supabase."
    echo "Erro detalhado:"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1"
fi

# Testar conexão usando JDBC URL
echo "Tentando conectar usando JDBC URL..."
JDBC_URL="${SUPABASE_DB_URL:-jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME}"
echo "JDBC URL: $JDBC_URL"

# Verificar se conseguimos resolver o nome do host
echo "Verificando resolução de DNS para $DB_HOST..."
nslookup "$DB_HOST" || echo "Não foi possível resolver o nome de host. Verifique sua conectividade de rede."

# Verificar porta
echo "Verificando conectividade na porta $DB_PORT..."
nc -zv "$DB_HOST" "$DB_PORT" || echo "Não foi possível conectar à porta. Verifique se a porta está aberta e acessível."

echo "Teste de conexão concluído." 