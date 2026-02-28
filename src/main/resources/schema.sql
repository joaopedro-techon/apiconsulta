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
