package ru.itmo.service;

import jakarta.inject.Inject;
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
import java.util.List;
import java.util.Map;

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
        TypedQuery<Worker> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        List<Worker> results = query.getResultList();
        return results;
    }

//    private List<Worker> findAllWorkersPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Worker> cq = cb.createQuery(Worker.class);
//        Root<Worker> root = cq.from(Worker.class);
//        root.fetch("organization", JoinType.LEFT);
//        root.fetch("person", JoinType.LEFT);
//
//        List<Predicate> predicates = new ArrayList<>();
//        if(filters != null && !filters.isEmpty()) {
//            for (Map.Entry<String, String> entry : filters.entrySet()) {
//                String field = entry.getKey();
//                String pattern = entry.getValue();
//
//                Expression<String> fieldAsString;
//                if (field.contains(".")) {
//                    String[] parts = field.split("\\.");
//                    fieldAsString = cb.function("TO_CHAR", String.class, root.get(parts[0]).get(parts[1]));
//                } else {
//                    fieldAsString = cb.function("TO_CHAR", String.class, root.get(field));
//                }
//                Predicate likePredicate = cb.like(fieldAsString, pattern);
//                predicates.add(likePredicate);
//            }
//        }
//
//        if (!predicates.isEmpty()) {
//            Predicate fullPredicate = cb.or(predicates.toArray(new Predicate[0]));
//            cq.where(fullPredicate);
//        }
//
//        if(sortField != null && !sortField.isEmpty() && sortDirection != null && !sortDirection.isEmpty()) {
//            Path<?> sortPath = sortField.contains(".")
//                    ? root.get(sortField.split("\\.")[0]).get(sortField.split("\\.")[1])
//                    : root.get(sortField);
//            cq = (sortDirection.equals("asc")) ? cq.orderBy(cb.asc(sortPath)) : cq.orderBy(cb.desc(sortPath));
//        }
//
//        cq.distinct(true);
//
//        TypedQuery<Worker> query = entityManager.createQuery(cq);
//        query.setFirstResult(page * pageSize);
//        query.setMaxResults(pageSize);
//
//        return query.getResultList();
//    }

    public Worker findById(Long id) {
        return entityManager.find(Worker.class, id);
    }



    public List<Worker> nameContains(String substring) {
        TypedQuery<Worker> q = entityManager.createQuery(
                "SELECT w FROM Worker w WHERE LOWER(w.name) LIKE LOWER(:sub)", Worker.class);
        q.setParameter("sub", "%" + substring + "%");
        return q.getResultList();
    }

    public List<Worker> nameStartsWith(String prefix) {
        TypedQuery<Worker> q = entityManager.createQuery(
                "SELECT w FROM Worker w WHERE LOWER(w.name) LIKE LOWER(:pref)", Worker.class);
        q.setParameter("pref", prefix + "%");
        return q.getResultList();
    }

    public List<Worker> ratingLessThan(Double value) {
        TypedQuery<Worker> q = entityManager.createQuery(
                "SELECT w FROM Worker w WHERE w.rating < :val", Worker.class);
        q.setParameter("val", value);
        return q.getResultList();
    }

    @Transactional
    public void hire(Long workerId, Long orgId) {
        Worker worker = entityManager.find(Worker.class, workerId);
        Organization org = entityManager.find(Organization.class, orgId);
        if (worker == null || org == null) throw new NoResultException();

        if (worker.getOrganization() != null) {
            worker.getOrganization().setEmployeesCount(worker.getOrganization().getEmployeesCount() - 1);
        }

        worker.setOrganization(org);
        org.setEmployeesCount(org.getEmployeesCount() + 1);
    }

    @Transactional
    public void transfer(Long workerId, Long newOrgId) {
        Worker worker = entityManager.find(Worker.class, workerId);
        Organization newOrg = entityManager.find(Organization.class, newOrgId);
        if (worker == null || newOrg == null) throw new NoResultException();

        Organization oldOrg = worker.getOrganization();
        if (oldOrg != null) {
            oldOrg.setEmployeesCount(oldOrg.getEmployeesCount() - 1);
        }

        worker.setOrganization(newOrg);
        newOrg.setEmployeesCount(newOrg.getEmployeesCount() + 1);
    }
}
