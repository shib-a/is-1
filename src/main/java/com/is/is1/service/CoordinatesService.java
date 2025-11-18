package com.is.is1.service;

import com.is.is1.model.Coordinates;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CoordinatesService {
    @PersistenceContext(unitName = "workerManagement")
    private EntityManager entityManager;

    private void createCoordinates(Coordinates coordinates) {
        entityManager.persist(coordinates);
    }
    private Coordinates updateCoordinates(Coordinates coordinates) {
        return entityManager.merge(coordinates);
    }
    private void deleteCoordinates(Coordinates coordinates) {
        entityManager.remove(coordinates);
    }
    private void findCoordinatesById(Long id) {
        entityManager.find(Coordinates.class, id);
    }
    private List<Coordinates> findAllCoordinatesPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Coordinates> cq = cb.createQuery(Coordinates.class);
        Root<Coordinates> root = cq.from(Coordinates.class);

        List<Predicate> predicates = new ArrayList<>();
        if(filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String field = entry.getKey();
                String pattern = entry.getValue();

                Expression<String> fieldAsString;
                fieldAsString = cb.function("TO_CHAR", String.class, root.get(field));
                Predicate likePredicate = cb.like(fieldAsString, pattern);
                predicates.add(likePredicate);
            }
        }
        Predicate fullPredicate = cb.or(predicates.toArray(new Predicate[0]));
        cq.where(fullPredicate);
        if(sortField != null && !sortField.isEmpty() && sortDirection != null && !sortDirection.isEmpty()) {
            cq = (sortDirection.equals("asc")) ? cq.orderBy(cb.asc(root.get(sortField))) : cq.orderBy(cb.desc(root.get(sortField)));
        }
        TypedQuery<Coordinates> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        List<Coordinates> results = query.getResultList();
        return results;
    }
}
