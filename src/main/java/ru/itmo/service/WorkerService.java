package ru.itmo.service;

import ru.itmo.model.Organization;
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
    @PersistenceContext(unitName = "workerManagement")
    private EntityManager entityManager;

    private void createWorker(Worker worker) {
        entityManager.persist(worker);
    }

    private Worker updateWorker(Worker worker) {
        return entityManager.merge(worker);
    }

    private void deleteWorker(Worker worker) {
        entityManager.remove(worker);
    }

    private Worker findWorkerById(Long id) {
        return entityManager.find(Worker.class, id);
    }

    private List<Worker> findAllWorkersPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Worker> cq = cb.createQuery(Worker.class);
        Root<Worker> root = cq.from(Worker.class);
        root.fetch("organization", JoinType.LEFT);
        root.fetch("person", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        if(filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String field = entry.getKey();
                String pattern = entry.getValue();

                Expression<String> fieldAsString;
                if (field.contains(".")) {
                    String[] parts = field.split("\\.");
                    fieldAsString = cb.function("TO_CHAR", String.class, root.get(parts[0]).get(parts[1]));
                } else {
                    fieldAsString = cb.function("TO_CHAR", String.class, root.get(field));
                }
                Predicate likePredicate = cb.like(fieldAsString, pattern);
                predicates.add(likePredicate);
            }
        }

        if (!predicates.isEmpty()) {
            Predicate fullPredicate = cb.or(predicates.toArray(new Predicate[0]));
            cq.where(fullPredicate);
        }

        if(sortField != null && !sortField.isEmpty() && sortDirection != null && !sortDirection.isEmpty()) {
            Path<?> sortPath = sortField.contains(".")
                    ? root.get(sortField.split("\\.")[0]).get(sortField.split("\\.")[1])
                    : root.get(sortField);
            cq = (sortDirection.equals("asc")) ? cq.orderBy(cb.asc(sortPath)) : cq.orderBy(cb.desc(sortPath));
        }

        cq.distinct(true);

        TypedQuery<Worker> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Worker findById(Long id) {
        return entityManager.find(Worker.class, id);
    }

    @Transactional
    public Worker create(Worker worker) {
        worker.setCreationDate(LocalDate.now());

        Organization org = worker.getOrganization();
        if (org.getId() == null) {
            entityManager.persist(org);
            org.setEmployeesCount(1L);
        } else {
            org = entityManager.find(Organization.class, org.getId());
            org.setEmployeesCount(org.getEmployeesCount() + 1);
        }
        worker.setOrganization(org);

        entityManager.persist(worker);
        return worker;
    }

    @Transactional
    public Worker update(Long id, Worker updated) {
        Worker existing = entityManager.find(Worker.class, id);
        if (existing == null) throw new NoResultException("Worker not found");

        existing.setName(updated.getName());
        existing.setCoordinates(updated.getCoordinates());
        existing.setSalary(updated.getSalary());
        existing.setRating(updated.getRating());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setPosition(updated.getPosition());

        if (!existing.getOrganization().getId().equals(updated.getOrganization().getId())) {
            Organization oldOrg = existing.getOrganization();
            Organization newOrg = entityManager.find(Organization.class, updated.getOrganization().getId());

            oldOrg.setEmployeesCount(oldOrg.getEmployeesCount() - 1);
            newOrg.setEmployeesCount(newOrg.getEmployeesCount() + 1);
            existing.setOrganization(newOrg);
        }

        existing.setPerson(updated.getPerson());

        return entityManager.merge(existing);
    }

    @Transactional
    public void delete(Long id) {
        Worker worker = entityManager.find(Worker.class, id);
        if (worker == null) throw new NoResultException();

        worker.getOrganization().setEmployeesCount(worker.getOrganization().getEmployeesCount() - 1);
        entityManager.remove(worker);
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
