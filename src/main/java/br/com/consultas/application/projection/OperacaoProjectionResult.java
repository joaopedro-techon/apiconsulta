package br.com.consultas.application.projection;

import java.util.List;
import java.util.Map;

/**
 * Resultado da projeção: operação, parcelas, originacao e amortizacoes como mapas (apenas campos solicitados).
 * Serializa diretamente para o JSON aninhado esperado pela API.
 */
public record OperacaoProjectionResult(
        Map<String, Object> operacao,
        List<Map<String, Object>> parcelas,
        Map<String, Object> originacao,
        List<Map<String, Object>> amortizacoes
) {
}
