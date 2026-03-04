package br.com.consultas.infrastructure.web.controller.response;

import br.com.consultas.domain.enums.StatusParcela;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * DTO de resposta da API para parcela.
 */
public record ParcelaResponse(
        @JsonProperty("numeroOperacao") Long numeroOperacao,
        @JsonProperty("numeroParcela") Integer numeroParcela,
        @JsonProperty("statusParcela") StatusParcela statusParcela,
        @JsonProperty("valorPrincipalParcela") BigDecimal valorPrincipalParcela,
        @JsonProperty("valorJuroPrincipalParcela") BigDecimal valorJuroPrincipalParcela
) {
}
