package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
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
    @Inject
    private EntityManager entityManager;
    public List<Coordinates> findAllCoordinatesPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Coordinates> cq = cb.createQuery(Coordinates.class);
        Root<Coordinates> root = cq.from(Coordinates.class);

        List<Predicate> predicates = new ArrayList<>();
        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String field = entry.getKey();
                String pattern = "%" + entry.getValue().toLowerCase() + "%";

                Expression<String> fieldExpr;
                try {
                    fieldExpr = root.get(field).as(String.class);
                    Predicate likePredicate = cb.like(cb.lower(fieldExpr), pattern);
                    predicates.add(likePredicate);
                } catch (IllegalArgumentException e) {
                    fieldExpr = cb.function("CAST", String.class, root.get(field), cb.literal("TEXT"));
                    Predicate likePredicate = cb.like(cb.lower(fieldExpr), pattern);
                    predicates.add(likePredicate);
                }
            }
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.or(predicates.toArray(new Predicate[0])));
        }

        if (sortField != null && !sortField.isEmpty() && sortDirection != null && !sortDirection.isEmpty()) {
            Order order = sortDirection.equals("asc") ? cb.asc(root.get(sortField)) : cb.desc(root.get(sortField));
            cq.orderBy(order);
        }

        TypedQuery<Coordinates> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Coordinates createCoordinates(Coordinates coordinates) {
        entityManager.getTransaction().begin();
        entityManager.persist(coordinates);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return coordinates;
    }

    public Coordinates updateCoordinates(Long id, Coordinates coordinates) {
        Coordinates existing = entityManager.find(Coordinates.class, id);
        if (existing == null) throw new NoResultException("Coordinates not found");
        entityManager.getTransaction().begin();
        coordinates.setId(id);
        var res = entityManager.merge(coordinates);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return res;
    }

    public void deleteCoordinates(Long id) {
        Coordinates coordinates = entityManager.find(Coordinates.class, id);
        if (coordinates == null) throw new NoResultException("Coordinates not found");
        entityManager.getTransaction().begin();
        entityManager.remove(coordinates);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }
    public Coordinates findCoordinatesById(Long id) {
        return entityManager.find(Coordinates.class, id);
    }
    public List<Coordinates> findAllCoordinatesTruncated() {
        TypedQuery<Coordinates> query = entityManager.createQuery("SELECT c FROM Coordinates c ORDER BY c.id DESC", Coordinates.class);
        query.setMaxResults(10);
        return query.getResultList();
    }
}
