package br.com.consultas.infrastructure.web.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Resposta da API: operação + lista de parcelas.
 */
public record OperacaoCreditoResponse(
        @JsonProperty("operacao") OperacaoResponse operacao,
        @JsonProperty("parcelas") List<ParcelaResponse> parcelas
) {
}
