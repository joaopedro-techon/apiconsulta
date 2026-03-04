package br.com.consultas.application.port.output;

import br.com.consultas.application.filter.FilterOptions;
import br.com.consultas.infrastructure.projection.ExpandOptions;
import br.com.consultas.infrastructure.projection.OperacaoProjectionResult;
import br.com.consultas.infrastructure.projection.SparseFieldSet;

import java.util.Optional;

/**
 * Port de saída para projeção de operação (Criteria API, expand e filter opcionais).
 */
public interface OperacaoProjectionPort {

    Optional<OperacaoProjectionResult> findProjection(Long numeroOperacao, SparseFieldSet fields, ExpandOptions expand, FilterOptions filterOptions);
}
