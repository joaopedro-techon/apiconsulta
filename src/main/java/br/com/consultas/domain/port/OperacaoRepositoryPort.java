package br.com.consultas.domain.port;

import br.com.consultas.domain.model.Operacao;

import java.util.Optional;

/**
 * Port de saída para persistência de operações (com parcelas).
 */
public interface OperacaoRepositoryPort {

    Optional<Operacao> findByNumeroOperacao(Long numeroOperacao);
}
