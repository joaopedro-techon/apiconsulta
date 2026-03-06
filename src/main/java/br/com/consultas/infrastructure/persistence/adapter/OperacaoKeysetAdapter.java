package br.com.consultas.infrastructure.persistence.adapter;

import br.com.consultas.application.port.output.OperacaoKeysetPort;
import br.com.consultas.infrastructure.persistence.entity.OperacaoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter de persistência para listagem paginada por keyset.
 * Query performática: WHERE numero_operacao &gt; :cursor ORDER BY numero_operacao ASC LIMIT n+1,
 * permitindo uso de índice na PK e custo O(1) por página.
 */
@Component
public class OperacaoKeysetAdapter implements OperacaoKeysetPort {

    private static final String JPQL = """
            SELECT o FROM OperacaoEntity o
            WHERE (:after IS NULL OR o.numeroOperacao > :after)
            ORDER BY o.numeroOperacao ASC
            """;

    private final EntityManager entityManager;

    public OperacaoKeysetAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public KeysetPageResult findPageAfter(Long afterNumeroOperacaoExclusive, int limit) {
        TypedQuery<OperacaoEntity> query = entityManager.createQuery(JPQL, OperacaoEntity.class)
                .setParameter("after", afterNumeroOperacaoExclusive)
                .setMaxResults(limit + 1); // fetch one extra to know if there is a next page

        List<OperacaoEntity> rows = query.getResultList();
        boolean hasMore = rows.size() > limit;
        List<OperacaoEntity> page = hasMore ? rows.subList(0, limit) : rows;

        Long nextCursorKey = hasMore ? page.get(page.size() - 1).getNumeroOperacao() : null;
        List<Map<String, Object>> data = page.stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        return new KeysetPageResult(data, nextCursorKey);
    }

    private Map<String, Object> toMap(OperacaoEntity o) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("numeroOperacao", o.getNumeroOperacao());
        map.put("status", o.getStatus() != null ? o.getStatus().name() : null);
        map.put("dataContratacao", o.getDataContratacao());
        map.put("codigoMeioCobranca", o.getCodigoMeioCobranca());
        return map;
    }
}
