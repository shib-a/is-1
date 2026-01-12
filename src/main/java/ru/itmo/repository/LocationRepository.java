package ru.itmo.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import ru.itmo.model.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class LocationRepository {
    @Inject
    private EntityManager entityManager;

    public List<Location> findAllPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Location> cq = cb.createQuery(Location.class);
        Root<Location> root = cq.from(Location.class);

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

        TypedQuery<Location> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Location findById(Long id) {
        return entityManager.find(Location.class, id);
    }

    public Location create(Location location) {
        entityManager.getTransaction().begin();
        entityManager.persist(location);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return location;
    }

    public Location update(Long id, Location location) {
        Location existing = entityManager.find(Location.class, id);
        if (existing == null) throw new NoResultException("Location not found");

        entityManager.getTransaction().begin();
        location.setId(id);
        var res = entityManager.merge(location);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return res;
    }

    public void delete(Location location) {
        entityManager.getTransaction().begin();
        entityManager.remove(location);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    public List<Location> findAllTruncated() {
        TypedQuery<Location> query = entityManager.createQuery("SELECT l FROM Location l ORDER BY l.id DESC", Location.class);
        query.setMaxResults(10);
        return query.getResultList();
    }
}
