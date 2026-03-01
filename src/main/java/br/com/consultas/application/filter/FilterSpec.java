package br.com.consultas.application.filter;

import java.util.List;

/**
 * Uma condição de filtro: recurso.campo + operador + valor(es).
 */
public record FilterSpec(
        String resource,
        String field,
        FilterOperator operator,
        String value,
        List<String> values
) {
    public boolean isInOperator() {
        return operator == FilterOperator.IN;
    }
}
