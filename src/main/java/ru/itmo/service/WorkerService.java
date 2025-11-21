package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import ru.itmo.model.Organization;
import ru.itmo.model.Person;
import ru.itmo.model.Worker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Data
public class WorkerService {

    @Inject
    private EntityManager entityManager;

    public Worker updateWorker(Long id, Worker worker) {
        Worker existing = entityManager.find(Worker.class, id);
        if (existing == null) throw new NoResultException("Person not found");


        entityManager.getTransaction().begin();
        worker.setId(id);
        var res = entityManager.merge(worker);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return res;
    }

    public Worker createWorker(Worker worker) {
        entityManager.getTransaction().begin();
        entityManager.persist(worker);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return worker;
    }

    public void deleteWorker(Long id) {
        Worker worker = entityManager.find(Worker.class, id);
        if (worker == null) throw new NoResultException("Address not found");

        entityManager.getTransaction().begin();
        entityManager.remove(worker);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    public Worker findWorkerById(Long id) {
        return entityManager.find(Worker.class, id);
    }

    public List<Worker> findAllWorkersPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
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

        List<Worker> results = query.getResultList();
        return results;
    }


    public Worker findById(Long id) {
        return entityManager.find(Worker.class, id);
    }



    public Map<Double, Long> groupBySalary() {
        return entityManager.createQuery(
                        "SELECT w.salary, COUNT(w) FROM Worker w GROUP BY w.salary ORDER BY w.salary",
                        Object[].class)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Double) arr[0],
                        arr -> (Long) arr[1]
                ));
    }
    public long countByEndDate(Date endDate) {
        if (endDate == null) {
            return entityManager.createQuery("SELECT COUNT(w) FROM Worker w WHERE w.endDate IS NULL", Long.class)
                    .getSingleResult();
        }
        return entityManager.createQuery("SELECT COUNT(w) FROM Worker w WHERE w.endDate = :endDate", Long.class)
                .setParameter("endDate", endDate)
                .getSingleResult();
    }

    public List<Worker> findByNameContaining(String substring) {
        String pattern = "%" + substring.toLowerCase() + "%";
        return entityManager.createQuery(
                        "SELECT w FROM Worker w WHERE LOWER(w.name) LIKE :pattern",
                        Worker.class)
                .setParameter("pattern", pattern)
                .getResultList();
    }

    public void indexSalaryForWorker(Long workerId, double coefficient) {
        Worker worker = entityManager.find(Worker.class, workerId);
        if (worker == null) throw new NotFoundException("Worker not found");
        entityManager.getTransaction().begin();
        worker.setSalary(worker.getSalary() * coefficient);
        entityManager.merge(worker);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    public void indexSalaryForOrganization(Long organizationId, double coefficient) {
        entityManager.getTransaction().begin();
        int updated = entityManager.createQuery(
                        "UPDATE Worker w SET w.salary = w.salary * :coef " +
                                "WHERE w.organization.id = :orgId")
                .setParameter("coef", coefficient)
                .setParameter("orgId", organizationId)
                .executeUpdate();
        entityManager.flush();
        entityManager.getTransaction().commit();
        if (updated == 0) {
            throw new NotFoundException("No workers found in organization or organization not found");
        }
    }

}
