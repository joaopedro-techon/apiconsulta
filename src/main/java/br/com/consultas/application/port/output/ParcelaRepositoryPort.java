package br.com.consultas.application.port.output;

import br.com.consultas.domain.models.ParcelaDomain;

import java.util.List;

/**
 * Port de saída para persistência de parcelas.
 * Use cases dependem desta interface; a implementação (Gateway) fica na infrastructure.
 */
public interface ParcelaRepositoryPort {

    List<ParcelaDomain> findByNumeroOperacao(Long numeroOperacao);
}
