package ru.itmo.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import ru.itmo.model.Coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CoordinatesRepository {
    @Inject
    private EntityManager entityManager;

    public List<Coordinates> findAllPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Coordinates> cq = cb.createQuery(Coordinates.class);
        Root<Coordinates> root = cq.from(Coordinates.class);

        List<Predicate> predicates = new ArrayList<>();
        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String field = entry.getKey();
                String value = entry.getValue();

                try {
                    Expression<?> fieldExpr = root.get(field);
                    Class<?> fieldType = fieldExpr.getJavaType();

                    if (fieldType == String.class) {
                        Predicate equalPredicate = cb.equal(cb.lower(fieldExpr.as(String.class)), value.toLowerCase());
                        predicates.add(equalPredicate);
                    } else if (fieldType.isEnum()) {
                        try {
                            Enum enumValue = Enum.valueOf((Class<Enum>) fieldType, value.toUpperCase());
                            Predicate equalPredicate = cb.equal(fieldExpr, enumValue);
                            predicates.add(equalPredicate);
                        } catch (IllegalArgumentException e) {
                        }
                    } else {
                        Predicate equalPredicate = cb.equal(fieldExpr.as(String.class), value);
                        predicates.add(equalPredicate);
                    }
                } catch (IllegalArgumentException e) {
                }
            }
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        if (sortField != null && !sortField.isEmpty() && sortDirection != null && !sortDirection.isEmpty()) {
            try {
                Order order = sortDirection.equals("asc") ? cb.asc(root.get(sortField)) : cb.desc(root.get(sortField));
                cq.orderBy(order);
            } catch (IllegalArgumentException e) {
                Order order = sortDirection.equals("asc") ? cb.asc(root.get("id")) : cb.desc(root.get("id"));
                cq.orderBy(order);
            }
        }

        TypedQuery<Coordinates> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Coordinates create(Coordinates coordinates) {
        entityManager.persist(coordinates);
        entityManager.flush();
        return coordinates;
    }

    public Coordinates findById(Long id) {
        return entityManager.find(Coordinates.class, id);
    }

    public Coordinates findByIdOrNull(Long id) {
        return entityManager.find(Coordinates.class, id);
    }

    public void persistInTransaction(Coordinates coordinates) {
        entityManager.persist(coordinates);
    }

    public Coordinates update(Long id, Coordinates coordinates) {
        Coordinates existing = entityManager.find(Coordinates.class, id);
        if (existing == null) throw new NoResultException("Coordinates not found");

        coordinates.setId(id);
        var res = entityManager.merge(coordinates);
        entityManager.flush();
        return res;
    }

    public void delete(Coordinates coordinates) {
        entityManager.remove(coordinates);
        entityManager.flush();
    }

    public void nullifyWorkerReferences(Long coordinatesId) {
        entityManager.createQuery("UPDATE Worker w SET w.coordinates = NULL WHERE w.coordinates.id = :coordsId")
                .setParameter("coordsId", coordinatesId)
                .executeUpdate();
        entityManager.flush();
    }

    public List<Coordinates> findAllTruncated() {
        TypedQuery<Coordinates> query = entityManager.createQuery("SELECT c FROM Coordinates c ORDER BY c.id DESC", Coordinates.class);
        query.setMaxResults(10);
        return query.getResultList();
    }
}
