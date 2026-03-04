package br.com.consultas.application.port.input;

import br.com.consultas.infrastructure.web.controller.response.ParcelaResponse;

import java.util.List;

/**
 * Port de entrada - caso de uso de listagem de parcelas por operação.
 */
public interface ObterParcelasPort {

    List<ParcelaResponse> obterParcelasPorOperacao(Long numeroOperacao);
}
