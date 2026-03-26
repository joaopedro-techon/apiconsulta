# Consultas Operações de Crédito

API REST em Spring Boot (Java 21) para consulta de operações de crédito em base **Amazon Aurora PostgreSQL**. Organizada com **Clean Architecture** (camadas concêntricas e Regra de Dependência).

📐 **Detalhes da arquitetura:** [ARCHITECTURE.md](ARCHITECTURE.md)

## Requisitos

- Java 21
- Maven 3.8+
- Aurora PostgreSQL (ou PostgreSQL compatível) para ambiente de produção

## Estrutura (Clean Architecture)

```
br.com.consultas/
├── domain/                    # Entities – regras de negócio da empresa (não depende de nada)
│   └── model/                 # Entidades de domínio (Operacao, Parcela, enums)
├── application/               # Use Cases – regras de aplicação
│   ├── dto/                   # Objetos de transferência (resposta da API)
│   ├── filter/                # Filtros (filter=parcelas.campo:op:valor)
│   ├── pagination/            # Keyset (cursor) – request, page, codec
│   ├── projection/            # Projeção (fields, expand, resultado)
│   ├── port/
│   │   ├── input/             # Contratos de entrada (use cases)
│   │   └── output/            # Contratos de saída (repositórios, projeção) – Gateways
│   └── usecase/               # Implementações dos casos de uso
└── infrastructure/            # Interface Adapters + Frameworks & Drivers
    ├── persistence/           # Gateways (adapters), JPA entities, repositórios, projeção
    └── web/                  # Controllers REST, tratamento de erros HTTP
```

**Regra de dependência:** dependências só apontam para dentro. `domain` não importa `application` nem `infrastructure`; `application` pode depender de `domain`; `infrastructure` implementa os ports e usa frameworks (Spring, JPA).

## Resiliência (Circuit Breaker, Rate Limiter, Bulkhead)

A API usa **Resilience4j** em torno do acesso ao banco (instância `operacoes-db`), aplicado nos use cases de consulta:

| Mecanismo | Função | Resposta quando ativado |
|-----------|--------|--------------------------|
| **Rate Limiter** | Limita requisições por segundo (ex.: 3000/s) | **429 Too Many Requests** |
| **Bulkhead** | Limita chamadas concorrentes (ex.: 20) para não esgotar pool/threads | **429 Too Many Requests** |
| **Circuit Breaker** | Após muitas falhas, interrompe chamadas ao banco por um tempo | **503 Service Unavailable** |

Configuração em `application.properties` (prefixos `resilience4j.ratelimiter`, `resilience4j.bulkhead`, `resilience4j.circuitbreaker`). Ajuste conforme carga esperada (ex.: ~5000 req/s):

- **Rate Limiter:** `limit-for-period` + `limit-refresh-period` (ex.: 3000 em 1s). `timeout-duration=0` = rejeitar na hora ao exceder.
- **Bulkhead:** `max-concurrent-calls` na ordem do connection pool (ex.: 20 com pool 10). `max-wait-duration=0` = não esperar vaga.
- **Circuit Breaker:** janela, taxa de falha e tempo em aberto conforme documentação no próprio arquivo.

## HTTP Outbound performático (Síncrono + Virtual Threads) para fanout

Para fazer chamada a serviços externos em paralelo (ex.: fanout com 8 serviços), o projeto adicionou um **HTTP client** performático com:

- **Pool de conexões** e **timeouts fail-fast** via `Apache HttpClient 5` em `external.http.*`.
- **Resiliência por chamada externa** via Resilience4j (instância `external-http`) em torno do adapter `ApacheHttpClientAdapter` (HTTP síncrono com Apache HttpClient 5).
- **Executor dedicado** para coordenar fanout (bean `fanoutExecutor`) utilizando **Virtual Threads**.

### Configuração (application.properties)

Valores principais (ajuste conforme SLA/latência média e volume):

- `external.http.max-connections`
- `external.http.connect-timeout-ms`
- `external.http.response-timeout-ms`
- `fanout.executor.core-pool-size` / `fanout.executor.max-pool-size` / `fanout.executor.queue-capacity`

E Resilience4j para chamadas externas:

- `resilience4j.circuitbreaker.instances.external-http.*`
- `resilience4j.ratelimiter.instances.external-http.*`
- `resilience4j.bulkhead.instances.external-http.*`

### Feign (exemplo de chamadas externas GET)

O projeto também adicionou **Spring Cloud OpenFeign** (com **OkHttp**) para chamadas externas tipadas.

- `ExternalSample1FeignClient` e `ExternalSample2FeignClient` (2 exemplos GET)
- `ExternalSamplesFeignAdapter` implementa `ExternalSamplesPort` e traduz erros para `ExternalServiceException`
- Essas chamadas também passam por `external-http` (circuit breaker/rate limiter/bulkhead)

Configuração:

- `external.sample1.base-url` (default: `https://postman-echo.com`)
- `external.sample2.base-url` (default: `https://postman-echo.com`)
- `spring.cloud.openfeign.okhttp.enabled=true`
- `spring.cloud.openfeign.client.config.default.connectTimeout` e `readTimeout`

### Como usar no fanout (modelo recomendado)

No use case (application), injete `HttpClientPort` e o executor `fanoutExecutor`, e dispare as 8 chamadas em paralelo com semântica fail-fast.

Padrão:

```java
// Exemplo (pseudo):
// CompletableFuture<R1> f1 = CompletableFuture.supplyAsync(() -> httpClient.get(url1, Tipo1.class), fanoutExecutor);
// ... até f8
// CompletableFuture.allOf(f1, ..., f8).join();
// Combine os resultados.
```

Se quiser fail-fast de forma simples, use:

```java
// tasks: lista de Callable<T> (cada uma faz uma chamada via httpClient.get)
// resultado = VirtualThreadFanout.failFast(tasks, fanoutExecutor, Duration.ofMillis(800));
```

## Tabelas (padrão tb_*)

| Tabela         | Descrição                    | Relação      |
|----------------|-------------------------------|-------------|
| `tb_operacao`  | Operações de crédito          | -           |
| `tb_parcela`   | Parcelas da operação          | N:1 com tb_operacao (FK: numero_operacao) |

## Executar localmente (H2)

Para testes locais sem PostgreSQL, use o perfil **h2**:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

- Banco H2 em memória, com dados iniciais carregados automaticamente
- Console H2 em http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:consultas_db`)

### Dados iniciais (perfil H2)

| numeroOperacao | status   | dataContratacao | codigoMeioCobranca (int) |
|---------------|----------|-----------------|---------------------------|
| 2024001       | ATIVA    | 2024-01-15      | 1                         |
| 2024002       | LIQUIDADA| 2024-02-20      | 2                         |
| 2024003       | ATIVA    | 2024-03-10      | 1                         |
| 2024004       | ATIVA    | 2024-04-05      | 3                         |
| 2024005       | CANCELADA| 2024-05-12      | 1                         |
| 2024006       | ATIVA    | 2024-06-18      | 2                         |
| 2024007       | LIQUIDADA| 2024-07-22      | 4                         |

Exemplo de teste:
```bash
curl http://localhost:8080/api/operacoes-credito/2024001
```

## Executar com Aurora PostgreSQL

```bash
mvn spring-boot:run
```

Configure via variáveis de ambiente:

| Variável     | Descrição          | Padrão   |
|-------------|--------------------|----------|
| `DB_HOST`   | Host do Aurora     | localhost |
| `DB_PORT`   | Porta              | 5432     |
| `DB_NAME`   | Nome do banco      | consultas |
| `DB_USERNAME` | Usuário          | postgres |
| `DB_PASSWORD` | Senha             | postgres |

Crie a tabela no Aurora executando `src/main/resources/schema.sql`.

## API REST

### GET Listar operações (paginação keyset/cursor)

Listagem performática com **keyset pagination** (cursor opaco): custo O(1) por página, sem OFFSET.

```
GET /api/operacoes-credito?limit=20&cursor=opaqueToken
Accept: application/json
```

| Parâmetro | Descrição |
|-----------|-----------|
| `limit`   | Tamanho da página (1 a 100). Padrão: 20. |
| `cursor`  | Cursor opaco da próxima página. Use o valor de `pagination.nextCursor` da resposta anterior. Omitir = primeira página. |

**Resposta 200 OK:**
```json
{
  "data": [
    {
      "numeroOperacao": 2024001,
      "status": "ATIVA",
      "dataContratacao": "2024-01-15",
      "codigoMeioCobranca": 1
    }
  ],
  "pagination": {
    "nextCursor": "eyJu...",
    "hasMore": true,
    "limit": 20
  }
}
```

Quando houver próxima página, o cabeçalho **Link** (RFC 5988) é enviado:
```
Link: </api/operacoes-credito?limit=20&cursor=eyJu...>; rel="next"
```

Próxima página: use o mesmo `GET` com `cursor=pagination.nextCursor` da resposta (ou siga o `Link`).

### GET Operação por número

```
GET /api/operacoes-credito/{numeroOperacao}
GET /api/operacoes-credito/{numeroOperacao}?expand=parcelas
GET /api/operacoes-credito/{numeroOperacao}?fields=operacao(numeroOperacao,status),parcelas(numeroParcela)&expand=parcelas
Accept: application/json
```

**Parâmetros (Criteria API no banco — uma query, JOIN só com expand):**

| Parâmetro | Descrição |
|-----------|------------|
| `expand`  | `parcelas` ou `parcela` — inclui parcelas na resposta. **Sem expand = só operação**. |
| `fields`  | Projeção: quais campos retornar. Formato estruturado ou com pontos. |
| `filter`  | Filtra parcelas (exige `expand=parcelas`). Vários filtros em AND. Formato: `parcelas.campo:operador:valor` |

**Filter — operadores:** `eq`, `neq`, `gt`, `gte`, `lt`, `lte`, `in`.  
Ex.: `filter=parcelas.statusParcela:eq:ATIVA`, `filter=parcelas.valorPrincipalParcela:gte:50`, `filter=parcelas.statusParcela:in:ATIVA,CANCELADA`, `filter=parcelas.statusParcela:neq:ATIVA`.  
Múltiplos: `?filter=parcelas.statusParcela:eq:ATIVA&filter=parcelas.valorPrincipalParcela:gte:50` ou `?filter=parcelas.statusParcela:eq:ATIVA;parcelas.valorPrincipalParcela:gte:50`

**Formato estruturado (recomendado):** `fields=operacao(numeroOperacao,status),parcelas(numeroParcela,valorPrincipalParcela)`  
**Com pontos:** `fields=operacao.numeroOperacao,parcelas.numeroParcela`

**Campos:** `operacao`: numeroOperacao, status, dataContratacao, codigoMeioCobranca. `parcelas`: numeroOperacao, numeroParcela, statusParcela, valorPrincipalParcela, valorJuroPrincipalParcela.

Exemplos:
- Sem parâmetros → só operação (todos os campos)
- `?expand=parcelas` → operação + parcelas (todos os campos)
- `?fields=operacao(numeroOperacao,status)&expand=parcelas` → operação (só número e status) + parcelas (todos)

**200 OK** – operação (e parcelas se `expand=parcelas`):
```json
{
  "operacao": {
    "numeroOperacao": 2024001,
    "status": "ATIVA",
    "dataContratacao": "2024-01-15",
    "codigoMeioCobranca": 1
  },
  "parcelas": [
    {
      "numeroOperacao": 2024001,
      "numeroParcela": 1,
      "statusParcela": "ATIVA",
      "valorPrincipalParcela": 1000.58,
      "valorJuroPrincipalParcela": 5.58
    }
  ]
}
```

**404 Not Found** – operação não encontrada.

### GET Parcelas da operação

```
GET /api/operacoes-credito/{numeroOperacao}/parcelas
Accept: application/json
```

**200 OK** – lista de parcelas (pode ser vazia):
```json
[
  {
    "numeroOperacao": 2024001,
    "numeroParcela": 1,
    "statusParcela": "ATIVA",
    "valorPrincipalParcela": 1000.00,
    "valorJuroPrincipalParcela": 50.00
  }
]
```

Campos: `numeroOperacao` (Long), `numeroParcela` (Integer), `statusParcela` (ATIVA | LIQUIDADA | CANCELADA | REPACTUADA), `valorPrincipalParcela` e `valorJuroPrincipalParcela` (BigDecimal).
