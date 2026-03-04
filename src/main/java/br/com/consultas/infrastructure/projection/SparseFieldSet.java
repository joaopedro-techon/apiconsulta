package br.com.consultas.infrastructure.projection;

import br.com.consultas.infrastructure.exceptions.InvalidFieldException;

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
    public static final Set<String> ORIGINACAO_FIELDS = Set.of(
            "numeroOperacao", "taxaJuros", "canalContratacao"
    );
    public static final Set<String> AMORTIZACOES_FIELDS = Set.of(
            "numeroOperacao", "numeroParcelaAmortizada", "dataRecebimento",
            "valorPrincipalAmortizado", "valorJurosPrincipalAmortizado", "indicadorValidadeAmortizacao"
    );

    /** Aceita operacao ou operacoes */
    private static final Set<String> RESOURCE_OPERACAO = Set.of("operacao", "operacoes");
    /** Aceita parcela ou parcelas */
    private static final Set<String> RESOURCE_PARCELAS = Set.of("parcela", "parcelas");
    /** Aceita originacao ou originacoes */
    private static final Set<String> RESOURCE_ORIGINACAO = Set.of("originacao", "originacoes");
    /** Aceita amortizacao ou amortizacoes */
    private static final Set<String> RESOURCE_AMORTIZACOES = Set.of("amortizacao", "amortizacoes");
    private static final Pattern STRUCTURED = Pattern.compile("(\\w+)\\(([^)]*)\\)");

    private static boolean isOperacao(String resource) {
        return resource != null && RESOURCE_OPERACAO.contains(resource.toLowerCase());
    }

    private static boolean isParcelas(String resource) {
        return resource != null && RESOURCE_PARCELAS.contains(resource.toLowerCase());
    }

    private static boolean isOriginacao(String resource) {
        return resource != null && RESOURCE_ORIGINACAO.contains(resource.toLowerCase());
    }

    private static boolean isAmortizacoes(String resource) {
        return resource != null && RESOURCE_AMORTIZACOES.contains(resource.toLowerCase());
    }

    private final List<String> operacaoFieldsOrdered;
    private final List<String> parcelasFieldsOrdered;
    private final List<String> originacaoFieldsOrdered;
    private final List<String> amortizacoesFieldsOrdered;

    private SparseFieldSet(List<String> operacaoFieldsOrdered, List<String> parcelasFieldsOrdered, List<String> originacaoFieldsOrdered, List<String> amortizacoesFieldsOrdered) {
        this.operacaoFieldsOrdered = Collections.unmodifiableList(operacaoFieldsOrdered);
        this.parcelasFieldsOrdered = Collections.unmodifiableList(parcelasFieldsOrdered);
        this.originacaoFieldsOrdered = Collections.unmodifiableList(originacaoFieldsOrdered);
        this.amortizacoesFieldsOrdered = Collections.unmodifiableList(amortizacoesFieldsOrdered);
    }

    /**
     * Formato estruturado: <code>operacao(numeroOperacao,status),parcelas(numeroParcela,valorPrincipalParcela)</code>
     * Ou com pontos: <code>operacao.numeroOperacao,parcelas.numeroParcela</code>
     * Ou vazio/null = todos os campos (conforme expand).
     */
    public static SparseFieldSet parse(String fieldsParam) {
        if (fieldsParam == null || fieldsParam.isBlank()) {
            return new SparseFieldSet(List.of(), List.of(), List.of(), List.of());
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
        List<String> originacaoFields = new ArrayList<>();
        List<String> amortizacoesFields = new ArrayList<>();
        Matcher m = STRUCTURED.matcher(rest);
        while (m.find()) {
            String resource = m.group(1).trim();
            String inner = m.group(2).trim();
            if (inner.isEmpty()) {
                if (isOperacao(resource)) {
                    operacaoFields.addAll(OPERACAO_FIELDS);
                } else if (isParcelas(resource)) {
                    parcelasFields.addAll(PARCELAS_FIELDS);
                } else if (isOriginacao(resource)) {
                    originacaoFields.addAll(ORIGINACAO_FIELDS);
                } else if (isAmortizacoes(resource)) {
                    amortizacoesFields.addAll(AMORTIZACOES_FIELDS);
                } else {
                    throw new InvalidFieldException("Recurso não permitido: " + resource + ". Use: operacao/operacoes, parcela/parcelas, originacao/originacoes, amortizacao/amortizacoes.");
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
                    } else if (isOriginacao(resource)) {
                        if (!ORIGINACAO_FIELDS.contains(field)) {
                            throw new InvalidFieldException("Campo não permitido: originacao." + field);
                        }
                        originacaoFields.add(field);
                    } else if (isAmortizacoes(resource)) {
                        if (!AMORTIZACOES_FIELDS.contains(field)) {
                            throw new InvalidFieldException("Campo não permitido: amortizacoes." + field);
                        }
                        amortizacoesFields.add(field);
                    } else {
                        throw new InvalidFieldException("Recurso não permitido: " + resource + ". Use: operacao/operacoes, parcela/parcelas, originacao/originacoes, amortizacao/amortizacoes.");
                    }
                }
            }
        }
        if (operacaoFields.isEmpty() && parcelasFields.isEmpty() && originacaoFields.isEmpty() && amortizacoesFields.isEmpty()) {
            throw new InvalidFieldException("Formato inválido. Use: operacao(campo1,campo2),parcela(campo1),originacao(campo1),amortizacoes(campo1).");
        }
        return new SparseFieldSet(
                new LinkedHashSet<>(operacaoFields).stream().toList(),
                new LinkedHashSet<>(parcelasFields).stream().toList(),
                new LinkedHashSet<>(originacaoFields).stream().toList(),
                new LinkedHashSet<>(amortizacoesFields).stream().toList()
        );
    }

    private static SparseFieldSet parseDotNotation(String rest) {
        List<String> operacaoFields = new ArrayList<>();
        List<String> parcelasFields = new ArrayList<>();
        List<String> originacaoFields = new ArrayList<>();
        List<String> amortizacoesFields = new ArrayList<>();
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
            } else if (isOriginacao(resource)) {
                if (field == null || field.isEmpty()) {
                    originacaoFields.addAll(ORIGINACAO_FIELDS);
                } else {
                    if (!ORIGINACAO_FIELDS.contains(field)) {
                        throw new InvalidFieldException("Campo não permitido: " + p);
                    }
                    originacaoFields.add(field);
                }
            } else if (isAmortizacoes(resource)) {
                if (field == null || field.isEmpty()) {
                    amortizacoesFields.addAll(AMORTIZACOES_FIELDS);
                } else {
                    if (!AMORTIZACOES_FIELDS.contains(field)) {
                        throw new InvalidFieldException("Campo não permitido: " + p);
                    }
                    amortizacoesFields.add(field);
                }
            } else {
                throw new InvalidFieldException("Campo não permitido: " + p + ". Use: operacao/operacoes, parcela/parcelas, originacao/originacoes, amortizacao/amortizacoes.");
            }
        }
        return new SparseFieldSet(
                new LinkedHashSet<>(operacaoFields).stream().toList(),
                new LinkedHashSet<>(parcelasFields).stream().toList(),
                new LinkedHashSet<>(originacaoFields).stream().toList(),
                new LinkedHashSet<>(amortizacoesFields).stream().toList()
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

    /** Campos da originacao (ordem estável). Se vazio e expand=originacao, usar todos. */
    public List<String> getOriginacaoFieldsOrdered() {
        return originacaoFieldsOrdered;
    }

    public List<String> getOriginacaoFieldsForExpand() {
        return originacaoFieldsOrdered.isEmpty() ? new ArrayList<>(ORIGINACAO_FIELDS) : originacaoFieldsOrdered;
    }

    /** Campos de cada amortização (ordem estável). Se vazio e expand=amortizacoes, usar todos. */
    public List<String> getAmortizacoesFieldsOrdered() {
        return amortizacoesFieldsOrdered;
    }

    public List<String> getAmortizacoesFieldsForExpand() {
        return amortizacoesFieldsOrdered.isEmpty() ? new ArrayList<>(AMORTIZACOES_FIELDS) : amortizacoesFieldsOrdered;
    }

    public boolean hasProjection() {
        return !operacaoFieldsOrdered.isEmpty() || !parcelasFieldsOrdered.isEmpty() || !originacaoFieldsOrdered.isEmpty() || !amortizacoesFieldsOrdered.isEmpty();
    }
}
