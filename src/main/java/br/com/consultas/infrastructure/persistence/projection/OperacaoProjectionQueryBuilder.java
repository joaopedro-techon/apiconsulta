package br.com.consultas.infrastructure.persistence.projection;

import br.com.consultas.application.projection.ExpandOptions;
import br.com.consultas.application.projection.OperacaoProjectionResult;
import br.com.consultas.application.projection.SparseFieldSet;
import br.com.consultas.infrastructure.persistence.entity.OperacaoEntity;
import br.com.consultas.infrastructure.persistence.entity.ParcelaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Projeção com Criteria API (Tuple + multiselect dinâmico).
 * Expand = parcelas só entra no JOIN quando solicitado.
 */
public final class OperacaoProjectionQueryBuilder {

    private final EntityManager entityManager;

    public OperacaoProjectionQueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Uma única query: se não houver linha, operação não existe (evita query extra de existência).
     */
    public Optional<OperacaoProjectionResult> execute(Long numeroOperacao, SparseFieldSet fields, ExpandOptions expand) {
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

        if (expand.isIncludeParcelas()) {
            var join = root.join("parcelas", JoinType.LEFT);
            parcelaFields = fields.getParcelasFieldsForExpand();
            for (String field : parcelaFields) {
                selections.add(join.get(field));
            }
            query.orderBy(cb.asc(join.get("numeroParcela")));
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

        return Optional.of(new OperacaoProjectionResult(operacaoMap, parcelasList));
    }

    private Map<String, Object> tupleToMap(Tuple tuple, List<String> fieldNames, int startIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < fieldNames.size(); i++) {
            map.put(fieldNames.get(i), tuple.get(i + startIndex));
        }
        return map;
    }
}
