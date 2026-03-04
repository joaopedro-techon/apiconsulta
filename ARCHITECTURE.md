# Clean Architecture

Este projeto segue a **Clean Architecture** (Uncle Bob), com camadas concêntricas e a **Regra de Dependência**: dependências só apontam para dentro. O núcleo não conhece frameworks, UI ou banco de dados.

## Camadas (de dentro para fora)

| Camada | Responsabilidade | Pacotes no projeto |
|--------|------------------|--------------------|
| **Entities** (Enterprise Business Rules) | Regras de negócio da empresa; modelos independentes de aplicação, UI ou banco. | `domain.model` |
| **Use Cases** (Application Business Rules) | Regras de aplicação; orquestram fluxo de dados e definem portas de entrada/saída. | `application.usecase`, `application.port.input`, `application.port.output`, `application.filter`, `application.projection` |
| **Interface Adapters** (Controllers, Presenters, Gateways) | Adaptam dados entre use cases e o mundo externo. Controllers recebem entrada; Presenters preparam saída; Gateways são as implementações dos ports de persistência. | `application.dto`, `infrastructure.web.controller`, `infrastructure.persistence.adapter` |
| **Frameworks & Drivers** (Web, DB, UI) | Ferramentas e frameworks: Spring Boot, JPA, banco de dados, tratamento de erros HTTP. | `infrastructure.persistence.entity`, `infrastructure.persistence.repository`, `infrastructure.persistence.projection`, `infrastructure.web.error` |

## Regra de Dependência

- **Domain** não depende de nada (zero imports de `application` ou `infrastructure`).
- **Application** depende apenas de **domain** (entidades e, se necessário, enums do domínio).
- **Infrastructure** depende de **application** (implementa ports) e pode usar **domain** (para mapear entidades de persistência para domínio).

Nenhum código de aplicação ou domínio importa Spring, JPA ou detalhes de HTTP.

## Fluxo de controle (ex.: GET operação)

1. **Controller** (Interface Adapter) recebe a requisição HTTP, parseia `fields`, `expand`, `filter` e chama o **Input Port** (use case).
2. **Use Case** (Application) orquestra: chama o **Output Port** (ex.: `OperacaoProjectionPort`) para obter dados.
3. **Gateway/Adapter** (Infrastructure) implementa o port (ex.: `OperacaoProjectionAdapter`) e usa o banco (Criteria API, JPA).
4. O resultado volta pelo port até o use case e depois ao controller, que devolve a resposta (JSON).

## Mapeamento de conceitos do diagrama

- **Entities** → `domain.model` (Operacao, Parcela, StatusOperacao, StatusParcela).
- **Use Case Input Port** → `application.port.input` (ObterOperacaoPort, ObterParcelasPort).
- **Use Case Output Port** → `application.port.output` (OperacaoProjectionPort, OperacaoRepositoryPort, ParcelaRepositoryPort).
- **Use Case Interactor** → `application.usecase` (ObterOperacaoUseCase, ObterParcelasUseCase).
- **Controller** → `infrastructure.web.controller`.
- **Gateway** (implementação) → `infrastructure.persistence.adapter`.
- **DB / External Interfaces** → `infrastructure.persistence.entity`, `infrastructure.persistence.repository`, `infrastructure.persistence.projection`.

## Boas práticas aplicadas

- **Ports** definidos na aplicação; implementações (adapters) na infraestrutura.
- **DTOs** e tipos de projeção (`OperacaoProjectionResult`, `SparseFieldSet`, etc.) na aplicação, para não vazar entidades JPA para a API.
- **Exceções de aplicação** (ex.: `InvalidFieldException`) na camada application; o `GlobalExceptionHandler` (infrastructure) as traduz em respostas HTTP.
