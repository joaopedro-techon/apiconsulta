package br.com.consultas.application.port.output;

import java.util.Map;

/**
 * Port de saída para exemplos de chamadas HTTP externas (Feign).
 * Usado apenas como referência/demonstração para fanout + resiliência.
 */
public interface ExternalSamplesPort {

    Map<String, Object> getSample1(String foo);

    Map<String, Object> getSample2(String bar);
}

