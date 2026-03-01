package br.com.consultas.application.filter;

import java.util.Arrays;

/**
 * Operadores suportados no parâmetro <code>filter</code>.
 */
public enum FilterOperator {
    EQ("eq"),
    NEQ("neq"),
    GT("gt"),
    GTE("gte"),
    LT("lt"),
    LTE("lte"),
    IN("in");

    private final String code;

    FilterOperator(String code) {
        this.code = code;
    }

    public static FilterOperator from(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Operador não informado");
        }
        String lower = code.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(op -> op.code.equals(lower))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Operador inválido: " + code + ". Use: eq, neq, gt, gte, lt, lte, in"));
    }
}
