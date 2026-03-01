package br.com.consultas.application.port.output;

import br.com.consultas.application.filter.FilterOptions;
import br.com.consultas.application.projection.ExpandOptions;
import br.com.consultas.application.projection.OperacaoProjectionResult;
import br.com.consultas.application.projection.SparseFieldSet;

import java.util.Optional;

/**
 * Port de saída para projeção de operação (Criteria API, expand e filter opcionais).
 */
public interface OperacaoProjectionPort {

    Optional<OperacaoProjectionResult> findProjection(Long numeroOperacao, SparseFieldSet fields, ExpandOptions expand, FilterOptions filterOptions);
}
