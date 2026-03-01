package br.com.consultas.application.projection;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Opções de expand: quais relacionamentos incluir na resposta.
 * Ex.: <code>expand=parcelas</code> traz as parcelas apenas quando solicitado.
 */
public final class ExpandOptions {

    /** Aceita singular ou plural: parcela, parcelas */
    private static final Set<String> EXPAND_PARCELAS = Set.of("parcela", "parcelas");

    private final boolean includeParcelas;

    private ExpandOptions(boolean includeParcelas) {
        this.includeParcelas = includeParcelas;
    }

    /**
     * Interpreta <code>expand=parcelas</code> ou <code>expand=parcela</code> (singular/plural aceitos).
     */
    public static ExpandOptions parse(String expandParam) {
        if (expandParam == null || expandParam.isBlank()) {
            return new ExpandOptions(false);
        }
        Set<String> requested = Arrays.stream(expandParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Set<String> invalid = requested.stream()
                .filter(r -> !EXPAND_PARCELAS.contains(r.toLowerCase()))
                .collect(Collectors.toSet());
        if (!invalid.isEmpty()) {
            throw new InvalidFieldException("Expand não permitido: " + String.join(", ", invalid) + ". Use: parcela ou parcelas.");
        }
        return new ExpandOptions(requested.stream().anyMatch(r -> EXPAND_PARCELAS.contains(r.toLowerCase())));
    }

    public boolean isIncludeParcelas() {
        return includeParcelas;
    }
}
