package ru.itmo.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import ru.itmo.model.ImportHistory;

import java.util.List;

@ApplicationScoped
public class ImportHistoryRepository {
    @Inject
    private EntityManager entityManager;

    public ImportHistory create(ImportHistory importHistory) {
        entityManager.persist(importHistory);
        entityManager.flush();
        return importHistory;
    }

    public ImportHistory update(ImportHistory importHistory) {
        ImportHistory result = entityManager.merge(importHistory);
        entityManager.flush();
        return result;
    }

    public List<ImportHistory> findAllPaged(int page, int pageSize, String sortField, String sortDirection) {
        String orderBy = sortField != null && !sortField.isEmpty() ? sortField : "timestamp";
        String direction = "asc".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";

        TypedQuery<ImportHistory> query = entityManager.createQuery(
                "SELECT ih FROM ImportHistory ih ORDER BY ih." + orderBy + " " + direction,
                ImportHistory.class
        );
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(ih) FROM ImportHistory ih", Long.class)
                .getSingleResult();
    }

    public ImportHistory findById(Long id) {
        return entityManager.find(ImportHistory.class, id);
    }
}
