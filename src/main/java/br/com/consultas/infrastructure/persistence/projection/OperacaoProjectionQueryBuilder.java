package br.com.consultas.infrastructure.persistence.projection;

import br.com.consultas.application.filter.FilterOperator;
import br.com.consultas.application.filter.FilterOptions;
import br.com.consultas.application.filter.FilterSpec;
import br.com.consultas.application.projection.InvalidFieldException;
import br.com.consultas.application.projection.ExpandOptions;
import br.com.consultas.application.projection.OperacaoProjectionResult;
import br.com.consultas.application.projection.SparseFieldSet;
import br.com.consultas.domain.model.StatusParcela;
import br.com.consultas.infrastructure.persistence.entity.AmortizacaoEntity;
import br.com.consultas.infrastructure.persistence.entity.OperacaoEntity;
import br.com.consultas.infrastructure.persistence.entity.OriginacaoEntity;
import br.com.consultas.infrastructure.persistence.entity.ParcelaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Path;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Projeção com Criteria API (Tuple + multiselect dinâmico).
 * Expand = parcelas só entra no JOIN quando solicitado.
 * Filtros de parcelas são aplicados no ON do JOIN (não no WHERE): se nenhuma parcela
 * corresponder ao filtro, a operação é retornada com lista de parcelas vazia.
 */
public final class OperacaoProjectionQueryBuilder {

    private final EntityManager entityManager;

    public OperacaoProjectionQueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Uma única query: se não houver linha, operação não existe.
     * Filtros aplicam-se às parcelas (ex.: statusParcela=ATIVA, valorPrincipalParcela>=50).
     */
    public Optional<OperacaoProjectionResult> execute(Long numeroOperacao, SparseFieldSet fields, ExpandOptions expand, FilterOptions filterOptions) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<OperacaoEntity> root = query.from(OperacaoEntity.class);

        List<String> operacaoFields = fields.getOperacaoFieldsOrdered();
        List<Selection<?>> selections = new ArrayList<>();
        for (String field : operacaoFields) {
            selections.add(root.get(field));
        }

        int operacaoCount = selections.size();
        List<String> parcelaFields = List.of();
        Join<OperacaoEntity, ParcelaEntity> join = null;

        if (expand.isIncludeParcelas()) {
            join = root.join("parcelas", JoinType.LEFT);
            parcelaFields = fields.getParcelasFieldsForExpand();
            for (String field : parcelaFields) {
                selections.add(join.get(field));
            }
            if (!filterOptions.getParcelasFilters().isEmpty()) {
                List<Predicate> onPredicates = new ArrayList<>();
                for (FilterSpec spec : filterOptions.getParcelasFilters()) {
                    onPredicates.add(buildParcelaPredicate(cb, join, spec));
                }
                join.on(onPredicates.toArray(new Predicate[0]));
            }
        }

        List<Order> orderList = new ArrayList<>();
        if (join != null) {
            orderList.add(cb.asc(join.get("numeroParcela")));
        }

        int originacaoStartIndex = selections.size();
        List<String> originacaoFields = List.of();
        if (expand.isIncludeOriginacao()) {
            Join<OperacaoEntity, OriginacaoEntity> originacaoJoin = root.join("originacao", JoinType.LEFT);
            originacaoFields = fields.getOriginacaoFieldsForExpand();
            for (String field : originacaoFields) {
                selections.add(originacaoJoin.get(field));
            }
        }

        int amortizacaoStartIndex = selections.size();
        List<String> amortizacaoFields = List.of();
        Join<OperacaoEntity, AmortizacaoEntity> amortizacaoJoin = null;
        if (expand.isIncludeAmortizacoes()) {
            amortizacaoJoin = root.join("amortizacoes", JoinType.LEFT);
            amortizacaoFields = fields.getAmortizacoesFieldsForExpand();
            for (String field : amortizacaoFields) {
                selections.add(amortizacaoJoin.get(field));
            }
            if (!filterOptions.getAmortizacoesFilters().isEmpty()) {
                List<Predicate> onPredicates = new ArrayList<>();
                for (FilterSpec spec : filterOptions.getAmortizacoesFilters()) {
                    onPredicates.add(buildAmortizacaoPredicate(cb, amortizacaoJoin, spec));
                }
                amortizacaoJoin.on(onPredicates.toArray(new Predicate[0]));
            }
            orderList.add(cb.asc(amortizacaoJoin.get("numeroParcelaAmortizada")));
            orderList.add(cb.asc(amortizacaoJoin.get("dataRecebimento")));
        }

        if (!orderList.isEmpty()) {
            query.orderBy(orderList);
        }
        query.multiselect(selections);
        query.where(cb.equal(root.get("numeroOperacao"), numeroOperacao));

        List<Tuple> results = entityManager.createQuery(query).getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> operacaoMap = tupleToMap(results.get(0), operacaoFields, 0);
        List<Map<String, Object>> parcelasList = null;

        if (expand.isIncludeParcelas() && !parcelaFields.isEmpty()) {
            parcelasList = new ArrayList<>();
            for (Tuple tuple : results) {
                Map<String, Object> parcelaMap = tupleToMap(tuple, parcelaFields, operacaoCount);
                if (parcelaMap.values().stream().anyMatch(v -> v != null)) {
                    parcelasList.add(parcelaMap);
                }
            }
        }

        Map<String, Object> originacaoMap = null;
        if (expand.isIncludeOriginacao() && !originacaoFields.isEmpty()) {
            originacaoMap = tupleToMap(results.get(0), originacaoFields, originacaoStartIndex);
            if (originacaoMap.values().stream().allMatch(v -> v == null)) {
                originacaoMap = null;
            }
        }

        List<Map<String, Object>> amortizacoesList = null;
        if (expand.isIncludeAmortizacoes() && !amortizacaoFields.isEmpty()) {
            Set<String> seenKeys = new TreeSet<>();
            amortizacoesList = new ArrayList<>();
            for (Tuple tuple : results) {
                Map<String, Object> amortizacaoMap = tupleToMap(tuple, amortizacaoFields, amortizacaoStartIndex);
                if (amortizacaoMap.values().stream().anyMatch(v -> v != null)) {
                    String key = amortizacaoMap.get("numeroOperacao") + "|" + amortizacaoMap.get("numeroParcelaAmortizada") + "|" + amortizacaoMap.get("dataRecebimento");
                    if (seenKeys.add(key)) {
                        amortizacoesList.add(amortizacaoMap);
                    }
                }
            }
        }

        return Optional.of(new OperacaoProjectionResult(operacaoMap, parcelasList, originacaoMap, amortizacoesList));
    }

    private Predicate buildParcelaPredicate(CriteriaBuilder cb, Join<OperacaoEntity, ParcelaEntity> join, FilterSpec spec) {
        String field = spec.field();
        Object value = spec.isInOperator() ? null : parseValue(field, spec.value());
        return switch (spec.operator()) {
            case EQ -> cb.equal(join.get(field), value);
            case NEQ -> cb.notEqual(join.get(field), value);
            case GT, GTE, LT, LTE -> buildComparisonPredicate(cb, join, field, value, spec.operator());
            case IN -> {
                List<Object> inValues = spec.values().stream()
                        .map(v -> parseValue(field, v))
                        .toList();
                yield join.get(field).in(inValues);
            }
        };
    }

    private Predicate buildAmortizacaoPredicate(CriteriaBuilder cb, Join<OperacaoEntity, AmortizacaoEntity> amortizacaoJoin, FilterSpec spec) {
        String field = spec.field();
        Object value = spec.isInOperator() ? null : parseAmortizacaoValue(field, spec.value());
        return switch (spec.operator()) {
            case EQ -> cb.equal(amortizacaoJoin.get(field), value);
            case NEQ -> cb.notEqual(amortizacaoJoin.get(field), value);
            case GT, GTE, LT, LTE -> buildAmortizacaoComparisonPredicate(cb, amortizacaoJoin, field, value, spec.operator());
            case IN -> {
                List<Object> inValues = spec.values().stream()
                        .map(v -> parseAmortizacaoValue(field, v))
                        .toList();
                yield amortizacaoJoin.get(field).in(inValues);
            }
        };
    }

    private Predicate buildAmortizacaoComparisonPredicate(CriteriaBuilder cb, Join<OperacaoEntity, AmortizacaoEntity> join, String field, Object value, FilterOperator op) {
        return switch (field) {
            case "valorPrincipalAmortizado", "valorJurosPrincipalAmortizado" -> {
                Path<BigDecimal> path = join.get(field);
                BigDecimal bd = (BigDecimal) value;
                yield switch (op) {
                    case GT -> cb.greaterThan(path, bd);
                    case GTE -> cb.greaterThanOrEqualTo(path, bd);
                    case LT -> cb.lessThan(path, bd);
                    case LTE -> cb.lessThanOrEqualTo(path, bd);
                    default -> throw new IllegalStateException("Operador não é comparação: " + op);
                };
            }
            case "numeroOperacao" -> {
                Path<Long> path = join.get(field);
                Long l = (Long) value;
                yield switch (op) {
                    case GT -> cb.greaterThan(path, l);
                    case GTE -> cb.greaterThanOrEqualTo(path, l);
                    case LT -> cb.lessThan(path, l);
                    case LTE -> cb.lessThanOrEqualTo(path, l);
                    default -> throw new IllegalStateException("Operador não é comparação: " + op);
                };
            }
            case "numeroParcelaAmortizada" -> {
                Path<Integer> path = join.get(field);
                Integer i = (Integer) value;
                yield switch (op) {
                    case GT -> cb.greaterThan(path, i);
                    case GTE -> cb.greaterThanOrEqualTo(path, i);
                    case LT -> cb.lessThan(path, i);
                    case LTE -> cb.lessThanOrEqualTo(path, i);
                    default -> throw new IllegalStateException("Operador não é comparação: " + op);
                };
            }
            case "dataRecebimento" -> {
                Path<LocalDate> path = join.get(field);
                LocalDate ld = (LocalDate) value;
                yield switch (op) {
                    case GT -> cb.greaterThan(path, ld);
                    case GTE -> cb.greaterThanOrEqualTo(path, ld);
                    case LT -> cb.lessThan(path, ld);
                    case LTE -> cb.lessThanOrEqualTo(path, ld);
                    default -> throw new IllegalStateException("Operador não é comparação: " + op);
                };
            }
            case "indicadorValidadeAmortizacao" -> throw new InvalidFieldException("Campo indicadorValidadeAmortizacao não suporta gt, gte, lt, lte. Use eq, neq ou in.");
            default -> throw new InvalidFieldException("Campo não suporta comparação (gt, gte, lt, lte): " + field);
        };
    }

    private Object parseAmortizacaoValue(String field, String value) {
        String v = value.trim();
        try {
            return switch (field) {
                case "numeroOperacao" -> Long.parseLong(v);
                case "numeroParcelaAmortizada" -> Integer.parseInt(v);
                case "dataRecebimento" -> LocalDate.parse(v);
                case "valorPrincipalAmortizado", "valorJurosPrincipalAmortizado" -> new BigDecimal(v.replace(",", "."));
                case "indicadorValidadeAmortizacao" -> Boolean.parseBoolean(v) || "true".equalsIgnoreCase(v) || "1".equals(v);
                default -> value;
            };
        } catch (NumberFormatException e) {
            throw new InvalidFieldException("Valor inválido para filtro em " + field + ": '" + value + "'.");
        } catch (IllegalArgumentException e) {
            throw new InvalidFieldException("Valor inválido para " + field + " (data no formato ISO): '" + value + "'.");
        }
    }

    private Predicate buildComparisonPredicate(CriteriaBuilder cb, Join<OperacaoEntity, ParcelaEntity> join, String field, Object value, FilterOperator op) {
        return switch (field) {
            case "valorPrincipalParcela", "valorJuroPrincipalParcela" -> {
                Path<BigDecimal> path = join.get(field);
                BigDecimal bd = (BigDecimal) value;
                yield switch (op) {
                    case GT -> cb.greaterThan(path, bd);
                    case GTE -> cb.greaterThanOrEqualTo(path, bd);
                    case LT -> cb.lessThan(path, bd);
                    case LTE -> cb.lessThanOrEqualTo(path, bd);
                    default -> throw new IllegalStateException("Operador não é comparação: " + op);
                };
            }
            case "numeroOperacao" -> {
                Path<Long> path = join.get(field);
                Long l = (Long) value;
                yield switch (op) {
                    case GT -> cb.greaterThan(path, l);
                    case GTE -> cb.greaterThanOrEqualTo(path, l);
                    case LT -> cb.lessThan(path, l);
                    case LTE -> cb.lessThanOrEqualTo(path, l);
                    default -> throw new IllegalStateException("Operador não é comparação: " + op);
                };
            }
            case "numeroParcela" -> {
                Path<Integer> path = join.get(field);
                Integer i = (Integer) value;
                yield switch (op) {
                    case GT -> cb.greaterThan(path, i);
                    case GTE -> cb.greaterThanOrEqualTo(path, i);
                    case LT -> cb.lessThan(path, i);
                    case LTE -> cb.lessThanOrEqualTo(path, i);
                    default -> throw new IllegalStateException("Operador não é comparação: " + op);
                };
            }
            default -> throw new InvalidFieldException("Campo não suporta comparação (gt, gte, lt, lte): " + field + ". Use eq, neq ou in.");
        };
    }

    private Object parseValue(String field, String value) {
        String v = value.trim();
        try {
            return switch (field) {
                case "numeroOperacao" -> Long.parseLong(v);
                case "numeroParcela" -> Integer.parseInt(v);
                case "statusParcela" -> StatusParcela.valueOf(v.toUpperCase());
                case "valorPrincipalParcela", "valorJuroPrincipalParcela" -> new BigDecimal(v.replace(",", "."));
                default -> value;
            };
        } catch (NumberFormatException e) {
            throw new InvalidFieldException("Valor inválido para filtro em " + field + ": '" + value + "'. Use número ou valor do enum (ATIVA, LIQUIDADA, CANCELADA, REPACTUADA).");
        } catch (IllegalArgumentException e) {
            throw new InvalidFieldException("Valor inválido para " + field + ": '" + value + "'. Status permitidos: ATIVA, LIQUIDADA, CANCELADA, REPACTUADA.");
        }
    }

    private Map<String, Object> tupleToMap(Tuple tuple, List<String> fieldNames, int startIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < fieldNames.size(); i++) {
            map.put(fieldNames.get(i), tuple.get(i + startIndex));
        }
        return map;
    }
}
