# Consultas Operações de Crédito

API REST em Spring Boot (Java 21) para consulta de operações de crédito em base **Amazon Aurora PostgreSQL**. Organizada com **Clean Architecture**.

## Requisitos

- Java 21
- Maven 3.8+
- Aurora PostgreSQL (ou PostgreSQL compatível) para ambiente de produção

## Estrutura (Clean Architecture)

```
br.com.consultas/
├── domain/                    # Camada mais interna - regras de negócio
│   ├── model/                 # Entidades de domínio
│   └── port/                  # Contratos (interfaces) de saída
├── application/               # Casos de uso
│   ├── dto/                   # Objetos de transferência
│   ├── port/input/            # Contratos de entrada (use cases)
│   └── usecase/               # Implementações dos casos de uso
└── infrastructure/            # Adaptadores externos
    ├── persistence/           # JPA, repositórios, banco de dados
    └── web/                   # Controllers REST
```

**Fluxo de dependência:** `infrastructure` → `application` → `domain` (o domínio não depende de nada).

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

### GET Operação por número

```
GET /api/operacoes-credito/{numeroOperacao}
Accept: application/json
```

**200 OK** – operação com lista de parcelas:
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
