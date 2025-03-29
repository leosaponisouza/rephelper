#!/bin/sh
set -e

# Configurações padrão da JVM
JAVA_OPTS=${JAVA_OPTS:-"-Xms256m -Xmx512m"}

# Configurações para ambiente de produção
PROD_OPTS="-XX:+UseContainerSupport -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError"

# Configurações específicas para Railway
if [ -n "$RAILWAY_ENVIRONMENT" ]; then
  echo "Executando em ambiente Railway: $RAILWAY_ENVIRONMENT"
  RAILWAY_OPTS="-Dserver.port=${PORT:-8080}"
fi

# Configurar o perfil ativo
PROFILE=${SPRING_PROFILES_ACTIVE:-"prod"}

# Iniciar a aplicação com todas as configurações
exec java $JAVA_OPTS $PROD_OPTS $RAILWAY_OPTS -jar app.jar --spring.profiles.active=$PROFILE 