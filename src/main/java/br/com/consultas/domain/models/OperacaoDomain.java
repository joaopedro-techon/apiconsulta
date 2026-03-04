package br.com.consultas.domain.models;

import br.com.consultas.domain.enums.StatusOperacao;

import java.time.LocalDate;
import java.util.List;

/**
 * Modelo de domínio de operação (com parcelas).
 * Camada Domain - regras de negócio centrais, sem dependências de frameworks.
 */
public record OperacaoDomain(
        Long numeroOperacao,
        StatusOperacao status,
        LocalDate dataContratacao,
        int codigoMeioCobranca,
        List<ParcelaDomain> parcelas
) {
}
