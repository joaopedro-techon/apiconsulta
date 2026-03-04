package br.com.consultas.application.port.output;

import br.com.consultas.domain.models.OperacaoDomain;

import java.util.Optional;

/**
 * Port de saída para persistência de operações (com parcelas).
 * Use cases dependem desta interface; a implementação (Gateway) fica na infrastructure.
 */
public interface OperacaoRepositoryPort {

    Optional<OperacaoDomain> findByNumeroOperacao(Long numeroOperacao);
}
