package ru.itmo.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import ru.itmo.model.Coordinates;
import ru.itmo.model.Organization;
import ru.itmo.model.Person;
import ru.itmo.model.Worker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class WorkerRepository {
    @Inject
    private EntityManager entityManager;

    public Worker findById(Long id) {
        return entityManager.find(Worker.class, id);
    }

    public Worker update(Worker worker) {
        Worker result = entityManager.merge(worker);
        entityManager.flush();
        return result;
    }

    public Worker create(Worker worker) {
        entityManager.persist(worker);
        entityManager.flush();
        return worker;
    }

    public void delete(Worker worker) {
        entityManager.remove(worker);
        entityManager.flush();
    }

    public void persistWorker(Worker worker) {
        entityManager.persist(worker);
    }


    public Organization getOrganizationReference(Long id) {
        return entityManager.getReference(Organization.class, id);
    }

    public Person getPersonReference(Long id) {
        return entityManager.getReference(Person.class, id);
    }

    public Coordinates getCoordinatesReference(Long id) {
        return entityManager.getReference(Coordinates.class, id);
    }

    public List<Worker> findAllPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Worker> cq = cb.createQuery(Worker.class);
        Root<Worker> root = cq.from(Worker.class);

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

        TypedQuery<Worker> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public List<Object[]> groupBySalary() {
        return entityManager.createQuery(
                        "SELECT w.salary, COUNT(w) FROM Worker w GROUP BY w.salary ORDER BY w.salary",
                        Object[].class)
                .getResultList();
    }

    public long countByEndDateNull() {
        return entityManager.createQuery("SELECT COUNT(w) FROM Worker w WHERE w.endDate IS NULL", Long.class)
                .getSingleResult();
    }

    public long countByEndDate(Date endDate) {
        return entityManager.createQuery("SELECT COUNT(w) FROM Worker w WHERE w.endDate = :endDate", Long.class)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .getSingleResult();
    }

    public List<Worker> findByNameContaining(String pattern) {
        return entityManager.createQuery(
                        "SELECT w FROM Worker w WHERE LOWER(w.name) LIKE :pattern",
                        Worker.class)
                .setParameter("pattern", pattern)
                .getResultList();
    }

    public int updateSalaryForOrganization(Long organizationId, double coefficient) {
        int updated = entityManager.createQuery(
                        "UPDATE Worker w SET w.salary = w.salary * :coef " +
                                "WHERE w.organization.id = :orgId")
                .setParameter("coef", coefficient)
                .setParameter("orgId", organizationId)
                .executeUpdate();
        entityManager.flush();
        return updated;
    }
}
