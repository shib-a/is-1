package com.is.is1.service;

import com.is.is1.common.Page;
import com.is.is1.model.Location;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class LocationService {
    @PersistenceContext(unitName = "workerManagement")
    private EntityManager entityManager;

    private void createLocation(Location location) {
        entityManager.persist(location);
    }
    private Location updateLocation(Location location) {
        return entityManager.merge(location);
    }
    private void deleteLocation(Location location) {
        entityManager.remove(location);
    }
    private Location findLocationById(Long id) {
        return entityManager.find(Location.class, id);
    }
    private List<Location> findAllLocationsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Location> cq = cb.createQuery(Location.class);
        Root<Location> root = cq.from(Location.class);

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
        TypedQuery<Location> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        List<Location> results = query.getResultList();
        return results;
    }

}
