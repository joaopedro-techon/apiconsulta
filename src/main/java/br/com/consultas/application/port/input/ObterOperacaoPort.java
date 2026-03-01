package br.com.consultas.application.port.input;

import br.com.consultas.application.projection.ExpandOptions;
import br.com.consultas.application.projection.SparseFieldSet;

import java.util.Optional;

/**
 * Port de entrada - consulta de operação com projeção e expand no banco.
 */
public interface ObterOperacaoPort {

    /**
     * @param expand expand=parcelas para incluir parcelas (JOIN só quando pedido)
     * @return OperacaoProjectionResult (sempre projeção; sem expand = só operação)
     */
    Optional<?> obterPorNumero(Long numeroOperacao, SparseFieldSet fields, ExpandOptions expand);
}
