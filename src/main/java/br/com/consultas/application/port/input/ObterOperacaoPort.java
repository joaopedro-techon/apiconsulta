package br.com.consultas.application.port.input;

import br.com.consultas.application.dto.OperacaoCreditoResponse;

import java.util.Optional;

/**
 * Port de entrada - caso de uso de consulta de operação (com parcelas).
 */
public interface ObterOperacaoPort {

    Optional<OperacaoCreditoResponse> obterPorNumero(Long numeroOperacao);
}
