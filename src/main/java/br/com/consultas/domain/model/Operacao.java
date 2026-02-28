package br.com.consultas.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Modelo de domínio de operação (com parcelas).
 * Camada Domain - regras de negócio centrais, sem dependências de frameworks.
 */
public record Operacao(
        Long numeroOperacao,
        StatusOperacao status,
        LocalDate dataContratacao,
        int codigoMeioCobranca,
        List<Parcela> parcelas
) {
}
