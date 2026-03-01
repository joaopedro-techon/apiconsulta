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
    /** Aceita singular ou plural: originacao, originacoes */
    private static final Set<String> EXPAND_ORIGINACAO = Set.of("originacao", "originacoes");
    /** Aceita singular ou plural: amortizacao, amortizacoes */
    private static final Set<String> EXPAND_AMORTIZACOES = Set.of("amortizacao", "amortizacoes");

    private final boolean includeParcelas;
    private final boolean includeOriginacao;
    private final boolean includeAmortizacoes;

    private ExpandOptions(boolean includeParcelas, boolean includeOriginacao, boolean includeAmortizacoes) {
        this.includeParcelas = includeParcelas;
        this.includeOriginacao = includeOriginacao;
        this.includeAmortizacoes = includeAmortizacoes;
    }

    /**
     * Interpreta <code>expand=parcelas,originacao,amortizacoes</code> (singular/plural aceitos).
     */
    public static ExpandOptions parse(String expandParam) {
        if (expandParam == null || expandParam.isBlank()) {
            return new ExpandOptions(false, false, false);
        }
        Set<String> requested = Arrays.stream(expandParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Set<String> valid = Set.of("parcela", "parcelas", "originacao", "originacoes", "amortizacao", "amortizacoes");
        Set<String> invalid = requested.stream()
                .filter(r -> !valid.contains(r.toLowerCase()))
                .collect(Collectors.toSet());
        if (!invalid.isEmpty()) {
            throw new InvalidFieldException("Expand não permitido: " + String.join(", ", invalid) + ". Use: parcela/parcelas, originacao/originacoes, amortizacao/amortizacoes.");
        }
        return new ExpandOptions(
                requested.stream().anyMatch(r -> EXPAND_PARCELAS.contains(r.toLowerCase())),
                requested.stream().anyMatch(r -> EXPAND_ORIGINACAO.contains(r.toLowerCase())),
                requested.stream().anyMatch(r -> EXPAND_AMORTIZACOES.contains(r.toLowerCase()))
        );
    }

    public boolean isIncludeParcelas() {
        return includeParcelas;
    }

    public boolean isIncludeOriginacao() {
        return includeOriginacao;
    }

    public boolean isIncludeAmortizacoes() {
        return includeAmortizacoes;
    }
}
