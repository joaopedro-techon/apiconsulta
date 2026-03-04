package br.com.consultas.domain.models;

import br.com.consultas.domain.enums.StatusParcela;

import java.math.BigDecimal;

/**
 * Modelo de domínio de parcela (relação 1:N com operação).
 */
public record ParcelaDomain(
        Long numeroOperacao,
        Integer numeroParcela,
        StatusParcela statusParcela,
        BigDecimal valorPrincipalParcela,
        BigDecimal valorJuroPrincipalParcela
) {
}
