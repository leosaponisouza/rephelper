#!/bin/sh
set -e

echo "Docker entrypoint iniciado"

# Verifica variáveis obrigatórias
if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
  echo "ERRO: Variável SPRING_PROFILES_ACTIVE não definida"
  echo "Definindo padrão como prod"
  export SPRING_PROFILES_ACTIVE=prod
fi

echo "Perfil ativo: $SPRING_PROFILES_ACTIVE"

# Se for ambiente de produção, verifica variáveis obrigatórias
if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
  echo "Verificando configurações para ambiente de produção..."

  # Verifica se as variáveis de banco de dados estão presentes
  if [ -z "$MYSQLHOST" ] || [ -z "$MYSQLPORT" ] || [ -z "$MYSQLDATABASE" ] || [ -z "$MYSQLUSER" ] || [ -z "$MYSQLPASSWORD" ]; then
    echo "AVISO: Algumas variáveis de banco de dados não estão configuradas!"
  else
    echo "Configuração de banco de dados OK"
  fi
  
  # Verifica se a variável do Firebase está configurada
  if [ -z "$FIREBASE_SERVICE_ACCOUNT_KEY" ]; then
    echo "AVISO: FIREBASE_SERVICE_ACCOUNT_KEY não está configurada. Autenticação pode falhar."
  else
    echo "Configuração do Firebase OK"
  fi
  
  # Copiar arquivo de ambiente de produção
  if [ -f ".env.prod" ]; then
    echo "Copiando .env.prod para .env"
    cp .env.prod .env
  fi
fi

echo "Iniciando aplicação com as seguintes configurações:"
echo "SPRING_PROFILES_ACTIVE: $SPRING_PROFILES_ACTIVE"
echo "DATABASE: $MYSQLDATABASE (Host: $MYSQLHOST:$MYSQLPORT)"

# Executar comando passado para o script
exec "$@" 