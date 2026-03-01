-- Dados iniciais para testes locais (perfil H2)
-- Executado automaticamente ao subir a aplicação com perfil h2

INSERT INTO tb_operacao (numero_operacao, status, data_contratacao, codigo_meio_cobranca) VALUES
(2024001, 'ATIVA', '2024-01-15', 1),
(2024002, 'LIQUIDADA', '2024-02-20', 2),
(2024003, 'ATIVA', '2024-03-10', 1),
(2024004, 'ATIVA', '2024-04-05', 3),
(2024005, 'CANCELADA', '2024-05-12', 1),
(2024006, 'ATIVA', '2024-06-18', 2),
(2024007, 'LIQUIDADA', '2024-07-22', 4);

INSERT INTO tb_parcela (numero_operacao, numero_parcela, status_parcela, valor_principal_parcela, valor_juro_principal_parcela) VALUES
(2024001, 1, 'ATIVA', 1000.00, 50.00),
(2024001, 2, 'ATIVA', 1000.00, 45.00),
(2024001, 3, 'LIQUIDADA', 1000.00, 40.00),
(2024002, 1, 'LIQUIDADA', 2000.00, 100.00),
(2024002, 2, 'LIQUIDADA', 2000.00, 90.00),
(2024003, 1, 'REPACTUADA', 500.00, 25.00),
(2024003, 2, 'ATIVA', 500.00, 20.00);

INSERT INTO tb_originacao (numero_operacao, taxa_juros, canal_contratacao) VALUES
(2024001, 1.25, 'DIGITAL'),
(2024002, 1.50, 'LOJA'),
(2024003, 1.10, 'DIGITAL'),
(2024004, 1.75, 'TELEFONE'),
(2024005, 1.30, 'DIGITAL'),
(2024006, 1.20, 'LOJA'),
(2024007, 1.60, 'TELEFONE');

INSERT INTO tb_amortizacao (numero_operacao, numero_parcela_amortizada, data_recebimento, valor_principal_amortizado, valor_juros_principal_amortizado, indicador_validade_amortizacao) VALUES
(2024001, 1, '2024-02-10', 500.00, 25.00, true),
(2024001, 1, '2024-03-10', 500.00, 25.00, true),
(2024001, 2, '2024-04-15', 1000.00, 45.00, true),
(2024002, 1, '2024-03-20', 2000.00, 100.00, true),
(2024002, 2, '2024-04-25', 2000.00, 90.00, true),
(2024003, 1, '2024-04-01', 250.00, 12.50, false),
(2024003, 2, '2024-05-10', 500.00, 20.00, true);
