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
        entityManager.getTransaction().begin();
        Worker result = entityManager.merge(worker);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return result;
    }

    public Worker create(Worker worker) {
        entityManager.getTransaction().begin();
        entityManager.persist(worker);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return worker;
    }

    public void delete(Worker worker) {
        entityManager.getTransaction().begin();
        entityManager.remove(worker);
        entityManager.flush();
        entityManager.getTransaction().commit();
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
                .setParameter("endDate", endDate)
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
        entityManager.getTransaction().begin();
        int updated = entityManager.createQuery(
                        "UPDATE Worker w SET w.salary = w.salary * :coef " +
                                "WHERE w.organization.id = :orgId")
                .setParameter("coef", coefficient)
                .setParameter("orgId", organizationId)
                .executeUpdate();
        entityManager.flush();
        entityManager.getTransaction().commit();
        return updated;
    }
}
