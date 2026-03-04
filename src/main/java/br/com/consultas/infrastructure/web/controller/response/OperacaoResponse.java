package br.com.consultas.infrastructure.web.controller.response;

import br.com.consultas.domain.enums.StatusOperacao;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * DTO da operação (objeto interno da resposta).
 */
public record OperacaoResponse(
        @JsonProperty("numeroOperacao") Long numeroOperacao,
        @JsonProperty("status") StatusOperacao status,
        @JsonProperty("dataContratacao") LocalDate dataContratacao,
        @JsonProperty("codigoMeioCobranca") int codigoMeioCobranca
) {
}
