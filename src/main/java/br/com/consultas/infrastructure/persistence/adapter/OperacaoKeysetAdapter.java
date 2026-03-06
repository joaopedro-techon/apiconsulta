package br.com.consultas.infrastructure.persistence.adapter;

import br.com.consultas.application.port.output.OperacaoKeysetPort;
import br.com.consultas.infrastructure.persistence.entity.OperacaoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter de persistência para listagem paginada por keyset.
 * Usa Criteria API com projeção (Tuple + multiselect): apenas as colunas necessárias,
 * sem materializar entidades nem carregar relacionamentos — foco em leitura performática.
 * Query: WHERE numero_operacao &gt; :cursor ORDER BY numero_operacao ASC LIMIT n+1.
 */
@Component
public class OperacaoKeysetAdapter implements OperacaoKeysetPort {

    private static final List<String> OPERACAO_FIELDS = List.of(
            "numeroOperacao", "status", "dataContratacao", "codigoMeioCobranca"
    );

    private final EntityManager entityManager;

    public OperacaoKeysetAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public KeysetPageResult findPageAfter(Long afterNumeroOperacaoExclusive, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<OperacaoEntity> root = query.from(OperacaoEntity.class);

        List<Selection<?>> selections = new ArrayList<>();
        for (String field : OPERACAO_FIELDS) {
            selections.add(root.get(field));
        }
        query.multiselect(selections);

        Predicate where = afterNumeroOperacaoExclusive == null
                ? cb.conjunction()
                : cb.greaterThan(root.get("numeroOperacao"), afterNumeroOperacaoExclusive);
        query.where(where);

        query.orderBy(cb.asc(root.get("numeroOperacao")));

        List<Tuple> rows = entityManager.createQuery(query)
                .setMaxResults(limit + 1)
                .getResultList();

        boolean hasMore = rows.size() > limit;
        List<Tuple> page = hasMore ? rows.subList(0, limit) : rows;

        Long nextCursorKey = null;
        if (hasMore && !page.isEmpty()) {
            nextCursorKey = (Long) page.get(page.size() - 1).get(0);
        }

        List<Map<String, Object>> data = page.stream()
                .map(tuple -> tupleToMap(tuple))
                .collect(Collectors.toList());

        return new KeysetPageResult(data, nextCursorKey);
    }

    private Map<String, Object> tupleToMap(Tuple tuple) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < OPERACAO_FIELDS.size(); i++) {
            Object value = tuple.get(i);
            if (value != null && value.getClass().isEnum()) {
                value = ((Enum<?>) value).name();
            }
            map.put(OPERACAO_FIELDS.get(i), value);
        }
        return map;
    }
}
