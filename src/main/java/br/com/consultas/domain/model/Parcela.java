package br.com.consultas.domain.model;

import java.math.BigDecimal;

/**
 * Modelo de domínio de parcela (relação 1:N com operação).
 */
public record Parcela(
        Long numeroOperacao,
        Integer numeroParcela,
        StatusParcela statusParcela,
        BigDecimal valorPrincipalParcela,
        BigDecimal valorJuroPrincipalParcela
) {
}
