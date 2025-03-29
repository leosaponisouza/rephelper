# RepHelper

Aplicação para gerenciamento de representantes comerciais.

## Requisitos

- Java 21
- Maven
- PostgreSQL

## Configuração para Ambiente de Desenvolvimento

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/rephelper.git
cd rephelper
```

2. Configure o arquivo `.env` baseado no `.env.example`:
```bash
cp .env.example .env
# Edite o arquivo .env com suas configurações
```

3. Execute a aplicação para desenvolvimento:
```bash
./mvnw spring-boot:run
```

A API estará disponível em: http://localhost:3000/api/v1

## Segurança e Variáveis de Ambiente

⚠️ **IMPORTANTE**: Nunca comite arquivos `.env` ou arquivos de configuração contendo informações sensíveis para o repositório!

A aplicação utiliza variáveis de ambiente para configurar todos os parâmetros sensíveis, incluindo:
- Credenciais de banco de dados
- Chaves JWT
- Credenciais do Firebase
- Configurações específicas de ambiente

### Gerenciamento de Variáveis de Ambiente

- Para desenvolvimento local, use o arquivo `.env` (não comitado)
- Para produção, configure variáveis de ambiente no servidor ou plataforma de deploy
- Use `.env.example` como template para configurar suas próprias variáveis

### Arquivos de Configuração

- `application.properties` - Configuração base com referências a variáveis de ambiente
- `application-dev.properties` - Configurações específicas para desenvolvimento
- `application-prod.properties` - Configurações específicas para produção

Para rodar em diferentes ambientes, use a variável `SPRING_PROFILES_ACTIVE`:
```bash
# Desenvolvimento
java -jar -Dspring.profiles.active=dev app.jar

# Produção
java -jar -Dspring.profiles.active=prod app.jar
```

## Configuração para Ambiente de Produção

1. Configure as variáveis de ambiente necessárias no servidor:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_HOST`
   - `DB_PORT`
   - `DB_NAME`
   - `DB_USER`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `JWT_EXPIRES_IN`
   - `FIREBASE_SERVICE_ACCOUNT_KEY`
   - `CORS_ALLOWED_ORIGINS`

2. Para criar o build para produção:
```bash
# Usando o script
chmod +x build-prod.sh
./build-prod.sh

# Ou manualmente
./mvnw clean package -DskipTests -Pprod
```

3. Para executar em produção:
```bash
java -jar -Dspring.profiles.active=prod target/rephelper-0.0.1-SNAPSHOT.jar
```

## Deploy em Serviços PaaS

### Heroku
```bash
# Certifique-se de ter o Heroku CLI instalado
heroku login
heroku create rephelper
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set DB_HOST=seu-host
# Configure as demais variáveis de ambiente
git push heroku main
```

### Railway
1. Faça login no [Railway](https://railway.app/) e crie um novo projeto.
2. Conecte seu repositório GitHub ou importe o código manualmente.
3. Configure todas as variáveis de ambiente necessárias no painel do Railway:
   - Acesse o serviço da aplicação > Variables
   - Adicione cada variável de ambiente listada no `.env.example`
   - Para serviços de banco de dados hospedados no Railway, use as variáveis geradas automaticamente:
     ```
     DB_HOST=${{Postgres.PGHOST}}
     DB_PORT=${{Postgres.PGPORT}}
     DB_NAME=${{Postgres.PGDATABASE}}
     DB_USER=${{Postgres.PGUSER}}
     DB_PASSWORD=${{Postgres.PGPASSWORD}}
     ```
4. Certifique-se de definir `SPRING_PROFILES_ACTIVE=prod`
5. Opcionalmente, adicione um serviço PostgreSQL ao seu projeto Railway para gerenciar o banco de dados junto com a aplicação.

O deploy será realizado automaticamente após as alterações serem commitadas no repositório.

## Documentação da API

Em ambiente de desenvolvimento, a documentação da API está disponível em:
- Swagger UI: http://localhost:3000/api/v1/swagger-ui.html
- OpenAPI JSON: http://localhost:3000/api/v1/api-docs

## Monitoramento
A aplicação utiliza Spring Boot Actuator para monitoramento em produção.

Endpoints disponíveis:
- /api/v1/actuator/health - Status da aplicação
- /api/v1/actuator/info - Informações sobre a aplicação
- /api/v1/actuator/metrics - Métricas da aplicação 