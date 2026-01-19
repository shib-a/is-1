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

        TypedQuery<Location> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Location findById(Long id) {
        return entityManager.find(Location.class, id);
    }

    public Location findByIdOrNull(Long id) {
        return entityManager.find(Location.class, id);
    }

    public void persistInTransaction(Location location) {
        entityManager.persist(location);
    }

    public Location create(Location location) {
        entityManager.persist(location);
        entityManager.flush();
        return location;
    }

    public Location update(Long id, Location location) {
        Location existing = entityManager.find(Location.class, id);
        if (existing == null) throw new NoResultException("Location not found");

        location.setId(id);
        var res = entityManager.merge(location);
        entityManager.flush();
        return res;
    }

    public void delete(Location location) {
        entityManager.remove(location);
        entityManager.flush();
    }

    public void nullifyReferences(Long locationId) {
        entityManager.createQuery("UPDATE Person p SET p.location = NULL WHERE p.location.id = :locId")
                .setParameter("locId", locationId)
                .executeUpdate();
        entityManager.createQuery("UPDATE Address a SET a.town = NULL WHERE a.town.id = :locId")
                .setParameter("locId", locationId)
                .executeUpdate();
        entityManager.flush();
    }

    public List<Location> findAllTruncated() {
        TypedQuery<Location> query = entityManager.createQuery("SELECT l FROM Location l ORDER BY l.id DESC", Location.class);
        query.setMaxResults(10);
        return query.getResultList();
    }
}
