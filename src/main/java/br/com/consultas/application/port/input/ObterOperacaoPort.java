package br.com.consultas.application.port.input;

import br.com.consultas.application.filter.FilterOptions;
import br.com.consultas.infrastructure.projection.ExpandOptions;
import br.com.consultas.infrastructure.projection.SparseFieldSet;

import java.util.Optional;

/**
 * Port de entrada - consulta de operação com projeção, expand e filter no banco.
 */
public interface ObterOperacaoPort {

    /**
     * @param expand  expand=parcelas para incluir parcelas
     * @param filters filtros nas parcelas (ex.: statusParcela=ATIVA, valorPrincipalParcela>=50)
     */
    Optional<?> obterPorNumero(Long numeroOperacao, SparseFieldSet fields, ExpandOptions expand, FilterOptions filters);
}
