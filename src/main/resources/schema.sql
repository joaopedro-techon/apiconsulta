-- Tabelas (padrão tb_*). Aurora Postgres / H2.
-- Execute este script no banco se as tabelas ainda não existirem.

CREATE TABLE IF NOT EXISTS tb_operacao (
    numero_operacao BIGINT NOT NULL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    data_contratacao DATE NOT NULL,
    codigo_meio_cobranca INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_parcela (
    numero_operacao BIGINT NOT NULL,
    numero_parcela INTEGER NOT NULL,
    status_parcela VARCHAR(20) NOT NULL,
    valor_principal_parcela DECIMAL(19, 4) NOT NULL,
    valor_juro_principal_parcela DECIMAL(19, 4) NOT NULL,
    PRIMARY KEY (numero_operacao, numero_parcela),
    CONSTRAINT fk_parcela_operacao FOREIGN KEY (numero_operacao) REFERENCES tb_operacao (numero_operacao)
);

CREATE TABLE IF NOT EXISTS tb_originacao (
    numero_operacao BIGINT NOT NULL PRIMARY KEY,
    taxa_juros DECIMAL(19, 6) NOT NULL,
    canal_contratacao VARCHAR(50) NOT NULL,
    CONSTRAINT fk_originacao_operacao FOREIGN KEY (numero_operacao) REFERENCES tb_operacao (numero_operacao)
);

CREATE TABLE IF NOT EXISTS tb_amortizacao (
    numero_operacao BIGINT NOT NULL,
    numero_parcela_amortizada INTEGER NOT NULL,
    data_recebimento DATE NOT NULL,
    valor_principal_amortizado DECIMAL(19, 4) NOT NULL,
    valor_juros_principal_amortizado DECIMAL(19, 4) NOT NULL,
    indicador_validade_amortizacao BOOLEAN NOT NULL,
    PRIMARY KEY (numero_operacao, numero_parcela_amortizada, data_recebimento),
    CONSTRAINT fk_amortizacao_operacao FOREIGN KEY (numero_operacao) REFERENCES tb_operacao (numero_operacao)
);
