# Template Service — Quarkus Native

Porta Quarkus do [template-service](../template-service) (Spring Boot Native). Mesma API, JWT, MySQL, soft delete e paginação — otimizado para **GraalVM native image**.

| Componente | Versão |
|---|---|
| Quarkus | **3.36.1** (latest) |
| Java | **25** |
| REST | `quarkus-rest` (JAX-RS) |
| ORM | Hibernate ORM Panache |
| DB | MySQL 8 (`quarkus-jdbc-mysql`) |
| Auth | JWT custom (compatível com Spring) + BCrypt |
| OpenAPI | SmallRye OpenAPI |
| Health | SmallRye Health (`/actuator/health`) |
| REST Client | MicroProfile REST Client (Google) |
| Native | GraalVM via `quarkus-maven-plugin` |

## Pré-requisitos

- Java 25+ (SDKMAN: `sdk install java 25-open`)
- Maven (wrapper incluído)
- Docker (para MySQL e imagens nativas)
- GraalVM/Mandrel (somente para build nativo local)

## Rodar local (JVM)

```bash
# Subir MySQL (tunado para load test — ver seção abaixo)
docker compose up db-mysql -d

# Rodar app (profile local → localhost:3306)
./mvnw quarkus:dev -Dquarkus.profile=local
```

API: `http://localhost:9090`  
Swagger: `http://localhost:9090/swagger-ui`  
Health: `http://localhost:9090/actuator/health`

**Credenciais:** `admin` / `admin`

```bash
# Login
curl -s -X POST http://localhost:9090/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin"}'

# Criar template (substitua TOKEN)
curl -s -X POST http://localhost:9090/template/ \
  -H "Authorization: Bearer TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"description":"teste quarkus"}'
```

## Testes

```bash
# Unit tests (Mockito, sem Spring context)
./mvnw test

# Integration tests (QuarkusTest + H2 in-memory)
./mvnw verify -DskipITs=false
```

## Build nativo

O projeto compila com **Java 25** (`maven.compiler.release=25`). O build nativo usa **container-build** (Mandrel JDK 25 via Docker) porque GraalVM/Mandrel local em Java 21 não lê class files 69.0.

**Pré-requisito:** Docker rodando (builder Mandrel JDK 25 + build da imagem final).

### Imagem Docker nativa (recomendado)

Um comando gera o executável nativo **e** a imagem Docker:

```bash
./mvnw clean package -Pnative,docker -DskipTests
```

| Profile | O que faz |
|---|---|
| `native` | Build nativo via container Mandrel JDK 25 |
| `docker` | Gera a imagem Docker com `quarkus-container-image-docker` |

**Nome da imagem gerada:**

```
docker.io/{group}/{name}:{tag}
```

Com a configuração atual do projeto:

| Propriedade | Valor | Origem |
|---|---|---|
| `quarkus.container-image.group` | `egripp/quarkus-native-template` | `application.properties` / profile `docker` |
| `quarkus.container-image.name` | `template-service` | `application.properties` / profile `docker` |
| `quarkus.container-image.tag` | `1.0.0` | `${project.version}` no profile `docker` |

**Imagem resultante:**

```
docker.io/egripp/quarkus-native-template/template-service:1.0.0
```

Verificar após o build:

```bash
docker images egripp/quarkus-native-template/template-service
```

Para mudar tag ou nome, altere `version` no `pom.xml` ou sobrescreva na linha de comando:

```bash
./mvnw clean package -Pnative,docker -DskipTests \
  -Dquarkus.container-image.tag=1.0.1-SNAPSHOT
```

### Executável local (sem imagem Docker)

```bash
./mvnw clean package -Pnative -DskipTests
./target/template-service-1.0.0-runner
```

### Imagem Docker via Dockerfile multi-stage (alternativa)

Se preferir o `Dockerfile` na raiz do projeto (build Maven dentro da imagem):

```bash
docker build -t egripp/quarkus-native-template/template-service:1.0.0 .
```

### Erro comum: `ApplicationImpl` / `UnsupportedClassVersionError`

Se aparecer `class file version 69.0` vs `65.0`, o native-image local está em Java 21 enquanto o código foi compilado com Java 25. Soluções:

1. **Recomendado:** usar `-Pnative` (já inclui `quarkus.native.container-build=true` no `pom.xml`)
2. **Alternativa:** instalar Mandrel/GraalVM **JDK 25** local e apontar `JAVA_HOME` para ele
3. **Não use** `--initialize-at-run-time=io.quarkus.runner.ApplicationImpl` — isso mascara o problema de versão

### Erro comum: `lstat /target: no such file or directory`

Ao usar `-Dquarkus.container-image.build=true` sem o profile `docker`, ou com `.dockerignore` excluindo `target/`, o build da imagem pode falhar. Use **`-Pnative,docker`** em vez de flags soltas.


### Docker Compose (app + MySQL)

O `docker-compose.yaml` inclui **MySQL tunado para load test** e a app nativa. O banco usa tmpfs (RAM), flags agressivas de throughput e desabilita o redo log na subida via entrypoint customizado.

**Pré-requisito:** criar a rede externa (uma vez):

```bash
docker network create template-service_default
```

**Subir stack completa:**

```bash
# Build da imagem nativa (gera egripp/quarkus-native-template/template-service:1.0.0)
./mvnw clean package -Pnative,docker -DskipTests

# Subir MySQL + app (app aguarda healthcheck do banco)
docker compose up -d
```

**Subir só o MySQL** (útil para dev local com `quarkus:dev -Dquarkus.profile=local`):

```bash
docker compose up db-mysql -d
```

**Recriar o banco do zero** (tmpfs perde dados ao remover o container):

```bash
docker compose down
docker rm -f db-mysql-quarkus
docker compose up db-mysql -d
```

| Serviço | Container | CPU | Memória | Porta |
|---|---|---|---|---|
| MySQL 8.0.33 | `db-mysql-quarkus` | 8 | 3G | 3306 |
| App nativa | `template-quarkus` | 1 | 256M | 9090 |

Credenciais MySQL: `root` / `example` — database `db`.

### MySQL para load test

Configuração **somente para benchmark** — sem durabilidade, não usar em produção.

O serviço `db-mysql` aplica:

- **tmpfs** em `/var/lib/mysql` (dados em RAM; somem ao remover o container)
- Logs desligados (`general_log`, `slow_query_log`, binlog)
- InnoDB relaxado (`flush_log_at_trx_commit=0`, `doublewrite=0`, `READ-UNCOMMITTED`)
- **Redo log desabilitado** automaticamente pelo script `docker/mysql/loadtest-entrypoint.sh`

Fluxo do entrypoint:

1. Sobe o MySQL via entrypoint oficial
2. Aguarda conexão TCP em `127.0.0.1:3306`
3. Executa `ALTER INSTANCE DISABLE INNODB REDO_LOG;`
4. Marca o container como pronto (`/tmp/mysql-loadtest-ready`)
5. A app só inicia após o healthcheck do banco passar

**Verificar se o redo log está desabilitado:**

```bash
# Sempre use -h127.0.0.1 (sem isso o cliente tenta socket e falha)
docker exec db-mysql-quarkus mysql -h127.0.0.1 -uroot -pexample \
  -e "SHOW GLOBAL STATUS LIKE 'Innodb_redo_log_enabled';"
# Esperado: OFF
```

**Verificar logs do bootstrap:**

```bash
docker logs db-mysql-quarkus 2>&1 | grep -E "redo|bootstrap|ERROR"
# Esperado: "InnoDB redo log disabled successfully."
```

**Ping no banco:**

```bash
docker exec db-mysql-quarkus mysqladmin ping -h127.0.0.1 -uroot -pexample --protocol=TCP
```

> **Nota:** `performance_schema` está desligado no compose de load test. Use `SHOW GLOBAL STATUS`, não consultas em `performance_schema`.

### Dicas para teste de carga (JMeter / k6)

- Rode o gerador de carga em **outra máquina** ou limite CPUs, para não competir com app e banco.
- `POST /template/` executa 2 queries (duplicate check + insert) — escrita satura o MySQL antes da app.
- Separe cenários: **GET** (leitura) vs **POST** (escrita) para identificar o gargalo.
- Para estressar só a aplicação (sem banco real), use o profile `test` (H2 in-memory) nos testes integrados.


## Profiles

| Profile | Uso | DB | Porta |
|---|---|---|---|
| `local` | dev na máquina | `localhost:3306` | 9090 |
| `dev` | docker-compose | `db-mysql:3306` | 9090 |
| `docker-local` | app em container, DB no host | `host.docker.internal:3306` | 9091 |
| `test` | testes integrados | H2 in-memory | — |

## Endpoints (paridade com Spring)

| Método | Path | Auth |
|---|---|---|
| `POST` | `/auth/login` | Público |
| `POST` | `/template/` | JWT |
| `PUT` | `/template/{id}` | JWT |
| `GET` | `/template/{id}` | JWT |
| `GET` | `/template?page=1&pageSize=10` | JWT |
| `DELETE` | `/template/{id}` | JWT |
| `GET` | `/template/google` | JWT |
| `GET` | `/actuator/health` | Público |
| `GET` | `/swagger-ui` | Público |

## Comparação Spring Native vs Quarkus Native

| Aspecto | Spring (template-service) | Quarkus (este projeto) |
|---|---|---|
| Build nativo | `native-maven-plugin` + buildpacks | `quarkus-maven-plugin -Dnative` |
| Imagem base | scratch / buildpack | UBI minimal / micro-image |
| Startup | ~ms (native) | ~ms (native) |
| Memória docker-compose | 1G limit | 256M limit (app) + 3G (MySQL load test) |
| Dev mode | spring-boot:run | `quarkus:dev` (hot reload) |
| GraalVM hints | Manual (`META-INF/native-image/`) | Automático (Quarkus extensions) |

## Estrutura

```
template-service-quarkus/
├── src/main/java/com/example/template/
│   ├── controller/       # REST endpoints
│   ├── service/          # Business logic
│   ├── persistence/      # Entity + Panache repository
│   ├── security/         # JWT + auth
│   ├── client/           # REST client (Google)
│   └── config/           # OpenAPI
├── docker/
│   └── mysql/
│       └── loadtest-entrypoint.sh   # Bootstrap: disable redo log
├── src/main/docker/      # Dockerfile.jvm, .native, .native-micro
├── docker-compose.yaml   # MySQL load test + app nativa
├── Dockerfile            # Multi-stage native build
└── pom.xml
```
