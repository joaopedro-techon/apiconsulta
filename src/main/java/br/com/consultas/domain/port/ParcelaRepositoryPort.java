package br.com.consultas.domain.port;

import br.com.consultas.domain.model.Parcela;

import java.util.List;

/**
 * Port de saída para persistência de parcelas.
 */
public interface ParcelaRepositoryPort {

    List<Parcela> findByNumeroOperacao(Long numeroOperacao);
}
