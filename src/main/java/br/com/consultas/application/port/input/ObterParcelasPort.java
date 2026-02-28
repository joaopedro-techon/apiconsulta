package br.com.consultas.application.port.input;

import br.com.consultas.application.dto.ParcelaResponse;

import java.util.List;

/**
 * Port de entrada - caso de uso de listagem de parcelas por operação.
 */
public interface ObterParcelasPort {

    List<ParcelaResponse> obterParcelasPorOperacao(Long numeroOperacao);
}
