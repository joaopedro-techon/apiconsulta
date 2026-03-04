package br.com.consultas.application.filter;

import br.com.consultas.infrastructure.exceptions.InvalidFieldException;
import br.com.consultas.infrastructure.projection.SparseFieldSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Conjunto de filtros para aplicar na consulta (ex.: filtrar parcelas por status ou valor).
 * Formato: <code>recurso.campo:operador:valor</code> — múltiplos filtros em AND.
 * <p>Ex.: <code>filter=parcelas.statusParcela:eq:ATIVA</code>, <code>filter=parcelas.valorPrincipalParcela:gte:50</code>, <code>filter=parcelas.statusParcela:in:ATIVA,CANCELADA</code>
 */
public final class FilterOptions {

    private static final Set<String> RESOURCE_PARCELAS = Set.of("parcela", "parcelas");
    private static final Set<String> RESOURCE_AMORTIZACOES = Set.of("amortizacao", "amortizacoes");

    private final List<FilterSpec> specs;

    private FilterOptions(List<FilterSpec> specs) {
        this.specs = Collections.unmodifiableList(specs);
    }

    /**
     * Interpreta o parâmetro <code>filter</code>. Múltiplos valores (AND): <code>?filter=x:eq:a&filter=y:gte:10</code>
     * Ou separados por ponto-e-vírgula: <code>?filter=parcelas.statusParcela:eq:ATIVA;parcelas.valorPrincipalParcela:gte:50</code>
     */
    public static FilterOptions parse(List<String> filterParams) {
        if (filterParams == null || filterParams.isEmpty()) {
            return new FilterOptions(List.of());
        }
        List<FilterSpec> list = new ArrayList<>();
        for (String param : filterParams) {
            if (param == null || param.isBlank()) continue;
            for (String part : param.split(";")) {
                String p = part.trim();
                if (p.isEmpty()) continue;
                list.add(parseOne(p));
            }
        }
        return new FilterOptions(list);
    }

    /**
     * Um único filtro: <code>recurso.campo:operador:valor</code>
     */
    public static FilterOptions parse(String filterParam) {
        if (filterParam == null || filterParam.isBlank()) {
            return new FilterOptions(List.of());
        }
        List<FilterSpec> list = new ArrayList<>();
        for (String part : filterParam.split(";")) {
            String p = part.trim();
            if (!p.isEmpty()) {
                list.add(parseOne(p));
            }
        }
        return new FilterOptions(list);
    }

    private static FilterSpec parseOne(String spec) {
        String[] parts = spec.split(":", 3);
        if (parts.length < 3) {
            throw new InvalidFieldException("Filtro inválido: '" + spec + "'. Use: recurso.campo:operador:valor (ex.: parcelas.statusParcela:eq:ATIVA)");
        }
        String resourceField = parts[0].trim();
        String opCode = parts[1].trim();
        String valuePart = parts[2].trim();

        int dot = resourceField.indexOf('.');
        if (dot <= 0 || dot == resourceField.length() - 1) {
            throw new InvalidFieldException("Filtro inválido: '" + spec + "'. Use: parcelas.campo ou amortizacoes.campo (ex.: parcelas.statusParcela, amortizacoes.indicadorValidadeAmortizacao)");
        }
        String resource = resourceField.substring(0, dot).trim();
        String field = resourceField.substring(dot + 1).trim();

        if (RESOURCE_PARCELAS.contains(resource.toLowerCase())) {
            if (!SparseFieldSet.PARCELAS_FIELDS.contains(field)) {
                throw new InvalidFieldException("Campo não permitido para filtro em parcelas: " + field);
            }
        } else if (RESOURCE_AMORTIZACOES.contains(resource.toLowerCase())) {
            if (!SparseFieldSet.AMORTIZACOES_FIELDS.contains(field)) {
                throw new InvalidFieldException("Campo não permitido para filtro em amortizacoes: " + field);
            }
        } else {
            throw new InvalidFieldException("Filtro só é permitido para recurso 'parcela'/'parcelas' ou 'amortizacao'/'amortizacoes'. Recebido: " + resource);
        }

        FilterOperator operator;
        try {
            operator = FilterOperator.from(opCode);
        } catch (IllegalArgumentException e) {
            throw new InvalidFieldException(e.getMessage());
        }

        if (operator == FilterOperator.IN) {
            List<String> values = Arrays.stream(valuePart.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
            if (values.isEmpty()) {
                throw new InvalidFieldException("Operador 'in' exige ao menos um valor (ex.: parcelas.statusParcela:in:ATIVA,CANCELADA)");
            }
            return new FilterSpec(resource, field, operator, null, values);
        }
        return new FilterSpec(resource, field, operator, valuePart, null);
    }

    public List<FilterSpec> getSpecs() {
        return specs;
    }

    public boolean hasFilters() {
        return !specs.isEmpty();
    }

    public List<FilterSpec> getParcelasFilters() {
        return specs.stream()
                .filter(s -> RESOURCE_PARCELAS.contains(s.resource().toLowerCase()))
                .toList();
    }

    public List<FilterSpec> getAmortizacoesFilters() {
        return specs.stream()
                .filter(s -> RESOURCE_AMORTIZACOES.contains(s.resource().toLowerCase()))
                .toList();
    }
}
