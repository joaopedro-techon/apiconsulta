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
