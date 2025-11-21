package ru.itmo.service;

import jakarta.persistence.NoResultException;
import ru.itmo.model.Coordinates;
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

    public Coordinates createCoordinates(Coordinates coordinates) {
        entityManager.persist(coordinates);
        return coordinates;
    }

    public Coordinates updateCoordinates(Long id, Coordinates coordinates) {

        Coordinates existing = entityManager.find(Coordinates.class, id);
        if (existing == null) throw new NoResultException("Coordinates not found");

        coordinates.setId(id);
        return entityManager.merge(coordinates);
    }

    public void deleteCoordinates(Long id) {
        Coordinates coordinates = entityManager.find(Coordinates.class, id);
        if (coordinates == null) throw new NoResultException("Coordinates not found");

        entityManager.remove(coordinates);
    }
    public Coordinates findCoordinatesById(Long id) {
        return entityManager.find(Coordinates.class, id);
    }
    public List<Coordinates> findAllCoordinatesPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
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
