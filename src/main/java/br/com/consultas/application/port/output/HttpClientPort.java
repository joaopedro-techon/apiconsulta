package br.com.consultas.application.port.output;

/**
 * Port de saída para chamadas HTTP a serviços externos.
 * Implementação fica na camada de infrastructure (gateway).
 */
public interface HttpClientPort {

    /**
     * Chamada HTTP GET com fail-fast (timeouts configurados na implementação).
     */
    <T> T get(String url, Class<T> responseType);
}

