package ru.itmo.service;

import jakarta.inject.Inject;
import ru.itmo.model.Coordinates;
import ru.itmo.model.Location;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class LocationService {
    @Inject
    private EntityManager entityManager;

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

        return query.getResultList();
    }

    public Location findLocationById(Long id) {
        return entityManager.find(Location.class, id);
    }

    public Location createLocation(Location location) {
        entityManager.getTransaction().begin();
        entityManager.persist(location);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return location;
    }

    public Location updateLocation(Long id, Location location) {
        Location existing = entityManager.find(Location.class, id);
        if (existing == null) throw new NoResultException("Location not found");

        entityManager.getTransaction().begin();
        location.setId(id);
        var res = entityManager.merge(location);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return res;
    }

    public void deleteLocation(Long id) {
        Location location = entityManager.find(Location.class, id);
        if (location == null) throw new NoResultException("Location not found");
        entityManager.getTransaction().begin();
        entityManager.remove(location);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }
    public List<Location> findAllLocationsTruncated() {
        TypedQuery<Location> query = entityManager.createQuery("SELECT l FROM Location l ORDER BY l.id DESC", Location.class);
        query.setMaxResults(10);
        return query.getResultList();
    }
}
