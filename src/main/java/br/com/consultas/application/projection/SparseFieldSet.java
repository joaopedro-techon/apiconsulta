package br.com.consultas.application.projection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modelo aninhado de sparse fieldsets.
 * Suporta formato estruturado: <code>operacao(numeroOperacao,status),parcelas(numeroParcela)</code>
 */
public final class SparseFieldSet {

    public static final Set<String> OPERACAO_FIELDS = Set.of(
            "numeroOperacao", "status", "dataContratacao", "codigoMeioCobranca"
    );
    public static final Set<String> PARCELAS_FIELDS = Set.of(
            "numeroOperacao", "numeroParcela", "statusParcela",
            "valorPrincipalParcela", "valorJuroPrincipalParcela"
    );

    /** Aceita operacao ou operacoes */
    private static final Set<String> RESOURCE_OPERACAO = Set.of("operacao", "operacoes");
    /** Aceita parcela ou parcelas */
    private static final Set<String> RESOURCE_PARCELAS = Set.of("parcela", "parcelas");
    private static final Pattern STRUCTURED = Pattern.compile("(\\w+)\\(([^)]*)\\)");

    private static boolean isOperacao(String resource) {
        return resource != null && RESOURCE_OPERACAO.contains(resource.toLowerCase());
    }

    private static boolean isParcelas(String resource) {
        return resource != null && RESOURCE_PARCELAS.contains(resource.toLowerCase());
    }

    private final List<String> operacaoFieldsOrdered;
    private final List<String> parcelasFieldsOrdered;

    private SparseFieldSet(List<String> operacaoFieldsOrdered, List<String> parcelasFieldsOrdered) {
        this.operacaoFieldsOrdered = Collections.unmodifiableList(operacaoFieldsOrdered);
        this.parcelasFieldsOrdered = Collections.unmodifiableList(parcelasFieldsOrdered);
    }

    /**
     * Formato estruturado: <code>operacao(numeroOperacao,status),parcelas(numeroParcela,valorPrincipalParcela)</code>
     * Ou com pontos: <code>operacao.numeroOperacao,parcelas.numeroParcela</code>
     * Ou vazio/null = todos os campos (conforme expand).
     */
    public static SparseFieldSet parse(String fieldsParam) {
        if (fieldsParam == null || fieldsParam.isBlank()) {
            return new SparseFieldSet(List.of(), List.of());
        }

        String rest = fieldsParam.trim();
        if (rest.contains("(")) {
            return parseStructured(rest);
        }
        return parseDotNotation(rest);
    }

    private static SparseFieldSet parseStructured(String rest) {
        List<String> operacaoFields = new ArrayList<>();
        List<String> parcelasFields = new ArrayList<>();
        Matcher m = STRUCTURED.matcher(rest);
        while (m.find()) {
            String resource = m.group(1).trim();
            String inner = m.group(2).trim();
            if (inner.isEmpty()) {
                if (isOperacao(resource)) {
                    operacaoFields.addAll(OPERACAO_FIELDS);
                } else if (isParcelas(resource)) {
                    parcelasFields.addAll(PARCELAS_FIELDS);
                } else {
                    throw new InvalidFieldException("Recurso não permitido: " + resource + ". Use: operacao/operacoes, parcela/parcelas.");
                }
            } else {
                for (String f : inner.split(",")) {
                    String field = f.trim();
                    if (isOperacao(resource)) {
                        if (!OPERACAO_FIELDS.contains(field)) {
                            throw new InvalidFieldException("Campo não permitido: operacao." + field);
                        }
                        operacaoFields.add(field);
                    } else if (isParcelas(resource)) {
                        if (!PARCELAS_FIELDS.contains(field)) {
                            throw new InvalidFieldException("Campo não permitido: parcelas." + field);
                        }
                        parcelasFields.add(field);
                    } else {
                        throw new InvalidFieldException("Recurso não permitido: " + resource + ". Use: operacao/operacoes, parcela/parcelas.");
                    }
                }
            }
        }
        if (operacaoFields.isEmpty() && parcelasFields.isEmpty()) {
            throw new InvalidFieldException("Formato inválido. Use: operacao(campo1,campo2),parcela(campo1) ou parcelas(campo1).");
        }
        return new SparseFieldSet(
                new LinkedHashSet<>(operacaoFields).stream().toList(),
                new LinkedHashSet<>(parcelasFields).stream().toList()
        );
    }

    private static SparseFieldSet parseDotNotation(String rest) {
        List<String> operacaoFields = new ArrayList<>();
        List<String> parcelasFields = new ArrayList<>();
        for (String path : rest.split(",")) {
            String p = path.trim();
            int dot = p.indexOf('.');
            String resource = dot < 0 ? p : p.substring(0, dot);
            String field = dot < 0 ? null : p.substring(dot + 1);
            if (isOperacao(resource)) {
                if (field == null || field.isEmpty()) {
                    operacaoFields.addAll(OPERACAO_FIELDS);
                } else {
                    if (!OPERACAO_FIELDS.contains(field)) {
                        throw new InvalidFieldException("Campo não permitido: " + p);
                    }
                    operacaoFields.add(field);
                }
            } else if (isParcelas(resource)) {
                if (field == null || field.isEmpty()) {
                    parcelasFields.addAll(PARCELAS_FIELDS);
                } else {
                    if (!PARCELAS_FIELDS.contains(field)) {
                        throw new InvalidFieldException("Campo não permitido: " + p);
                    }
                    parcelasFields.add(field);
                }
            } else {
                throw new InvalidFieldException("Campo não permitido: " + p + ". Use: operacao/operacoes, parcela/parcelas.");
            }
        }
        return new SparseFieldSet(
                new LinkedHashSet<>(operacaoFields).stream().toList(),
                new LinkedHashSet<>(parcelasFields).stream().toList()
        );
    }

    /** Campos da operação (ordem estável para Tuple). Se vazio, usar todos. */
    public List<String> getOperacaoFieldsOrdered() {
        return operacaoFieldsOrdered.isEmpty() ? new ArrayList<>(OPERACAO_FIELDS) : operacaoFieldsOrdered;
    }

    /** Campos de cada parcela (ordem estável). Se vazio e expand=parcelas, usar todos. */
    public List<String> getParcelasFieldsOrdered() {
        return parcelasFieldsOrdered;
    }

    public List<String> getParcelasFieldsForExpand() {
        return parcelasFieldsOrdered.isEmpty() ? new ArrayList<>(PARCELAS_FIELDS) : parcelasFieldsOrdered;
    }

    public boolean hasProjection() {
        return !operacaoFieldsOrdered.isEmpty() || !parcelasFieldsOrdered.isEmpty();
    }
}
