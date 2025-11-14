package com.is.is1.service;

import com.is.is1.common.Page;
import com.is.is1.model.Coordinates;
import com.is.is1.model.Organization;
import com.is.is1.model.Person;
import com.is.is1.model.Worker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
@ApplicationScoped
public class WorkerService {
    @PersistenceContext(unitName = "workerManagement")
    private EntityManager em;
    public Page<Worker> findAllPaged(int page, int size, String sortField, String sortDir, String filter) {

        // === Основной запрос БЕЗ алиасов на FETCH JOIN (работает в Hibernate 6 + strict compliance) ===
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT DISTINCT w FROM Worker w ");
        jpql.append("LEFT JOIN FETCH w.organization ");
        jpql.append("LEFT JOIN FETCH w.person ");
        jpql.append("WHERE 1 = 1 = 1");

        if (filter != null && !filter.isBlank()) {
            String pattern = "%" + filter.toLowerCase() + "%";
            jpql.append("AND (");
            jpql.append("  LOWER(w.name) LIKE :pattern OR ");
            jpql.append("  LOWER(COALESCE(w.organization.fullName, '')) LIKE :pattern OR ");
            jpql.append("  LOWER(COALESCE(w.person.passportID, '')) LIKE :pattern ");
            jpql.append(")");
        }

        // Сортировка — полные пути (алиасов нет)
        String orderBy = switch (sortField) {
            case "organizationFullName" -> "w.organization.fullName";
            case "personPassport"       -> "w.person.passportID";
            case "name"                 -> "w.name";
            case "salary"               -> "w.salary";
            case "rating"              -> "w.rating";
            case "creationDate"         -> "w.creationDate";
            case "position"             -> "w.position";
            default                     -> "w.id";
        };

        jpql.append(" ORDER BY ").append(orderBy)
                .append(" ")
                .append("desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC");

        TypedQuery<Worker> query = em.createQuery(jpql.toString(), Worker.class);

        if (filter != null && !filter.isBlank()) {
            query.setParameter("pattern", "%" + filter.toLowerCase() + "%");
        }

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        List<Worker> content = query.getResultList();

        // === Подсчёт количества (обычные JOIN с алиасами — разрешено) ===
        StringBuilder countJpql = new StringBuilder();
        countJpql.append("SELECT COUNT(DISTINCT w) FROM Worker w ");
        countJpql.append("LEFT JOIN w.organization org ");
        countJpql.append("LEFT JOIN w.person p ");
        countJpql.append("WHERE 1 = 1 ");

        if (filter != null && !filter.isBlank()) {
            countJpql.append("AND (");
            countJpql.append("  LOWER(w.name) LIKE :pattern OR ");
            countJpql.append("  LOWER(COALESCE(org.fullName, '')) LIKE :pattern OR ");
            countJpql.append("  LOWER(COALESCE(p.passportID, '')) LIKE :pattern ");
            countJpql.append(")");
        }

        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

        if (filter != null && !filter.isBlank()) {
            countQuery.setParameter("pattern", "%" + filter.toLowerCase() + "%");
        }

        long total = countQuery.getSingleResult();

        return new Page<>(content, total, page, size);
    }

    public Worker findById(Long id) {
        return em.find(Worker.class, id);
    }

    @Transactional
    public Worker create(Worker worker) {
        worker.setCreationDate(LocalDate.now());

        Organization org = worker.getOrganization();
        if (org.getId() == null) {
            em.persist(org);
            org.setEmployeesCount(1L);
        } else {
            org = em.find(Organization.class, org.getId());
            org.setEmployeesCount(org.getEmployeesCount() + 1);
        }
        worker.setOrganization(org);

        em.persist(worker);
        return worker;
    }

    @Transactional
    public Worker update(Long id, Worker updated) {
        Worker existing = em.find(Worker.class, id);
        if (existing == null) throw new NoResultException("Worker not found");

        existing.setName(updated.getName());
        existing.setCoordinates(updated.getCoordinates());
        existing.setSalary(updated.getSalary());
        existing.setRating(updated.getRating());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setPosition(updated.getPosition());

        // Смена организации с пересчётом employeesCount
        if (!existing.getOrganization().getId().equals(updated.getOrganization().getId())) {
            Organization oldOrg = existing.getOrganization();
            Organization newOrg = em.find(Organization.class, updated.getOrganization().getId());

            oldOrg.setEmployeesCount(oldOrg.getEmployeesCount() - 1);
            newOrg.setEmployeesCount(newOrg.getEmployeesCount() + 1);
            existing.setOrganization(newOrg);
        }

        existing.setPerson(updated.getPerson()); // orphanRemoval + cascade сделают всё

        return em.merge(existing);
    }

    @Transactional
    public void delete(Long id) {
        Worker worker = em.find(Worker.class, id);
        if (worker == null) throw new NoResultException();

        worker.getOrganization().setEmployeesCount(worker.getOrganization().getEmployeesCount() - 1);
        em.remove(worker);
    }

    // ==================== Специальные операции — TypedQuery ====================

    public List<Worker> nameContains(String substring) {
        TypedQuery<Worker> q = em.createQuery(
                "SELECT w FROM Worker w WHERE LOWER(w.name) LIKE LOWER(:sub)", Worker.class);
        q.setParameter("sub", "%" + substring + "%");
        return q.getResultList();
    }

    public List<Worker> nameStartsWith(String prefix) {
        TypedQuery<Worker> q = em.createQuery(
                "SELECT w FROM Worker w WHERE LOWER(w.name) LIKE LOWER(:pref)", Worker.class);
        q.setParameter("pref", prefix + "%");
        return q.getResultList();
    }

    public List<Worker> ratingLessThan(Double value) {
        TypedQuery<Worker> q = em.createQuery(
                "SELECT w FROM Worker w WHERE w.rating < :val", Worker.class);
        q.setParameter("val", value);
        return q.getResultList();
    }

    @Transactional
    public void hire(Long workerId, Long orgId) {
        Worker worker = em.find(Worker.class, workerId);
        Organization org = em.find(Organization.class, orgId);
        if (worker == null || org == null) throw new NoResultException();

        // Если у работника уже была организация — уменьшаем счётчик
        if (worker.getOrganization() != null) {
            worker.getOrganization().setEmployeesCount(worker.getOrganization().getEmployeesCount() - 1);
        }

        worker.setOrganization(org);
        org.setEmployeesCount(org.getEmployeesCount() + 1);
    }

    @Transactional
    public void transfer(Long workerId, Long newOrgId) {
        Worker worker = em.find(Worker.class, workerId);
        Organization newOrg = em.find(Organization.class, newOrgId);
        if (worker == null || newOrg == null) throw new NoResultException();

        Organization oldOrg = worker.getOrganization();
        if (oldOrg != null) {
            oldOrg.setEmployeesCount(oldOrg.getEmployeesCount() - 1);
        }

        worker.setOrganization(newOrg);
        newOrg.setEmployeesCount(newOrg.getEmployeesCount() + 1);
    }
}