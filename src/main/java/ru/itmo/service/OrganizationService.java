package ru.itmo.service;

import ru.itmo.model.Organization;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class OrganizationService {
    @PersistenceContext(unitName = "workerManagement")
    private EntityManager em;

    private void createOrganization(Organization organization) {
        em.persist(organization);
    }

    private Organization updateOrganization(Organization organization) {
        return em.merge(organization);
    }

    private void deleteOrganization(Organization organization) {
        em.remove(organization);
    }

    private Organization findOrganizationById(Long id) {
        return em.find(Organization.class, id);
    }

    private List<Organization> findAllOrganizationsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Organization> cq = cb.createQuery(Organization.class);
        Root<Organization> root = cq.from(Organization.class);

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
        TypedQuery<Organization> query = em.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        List<Organization> results = query.getResultList();
        return results;
    }

    public List<Organization> findAll() {
        return em.createQuery("SELECT o FROM Organization o", Organization.class)
                .getResultList();
    }

    @Transactional
    public void deleteWithReassign(Long id, Long newOrgId) {
        Organization org = em.find(Organization.class, id);
        if (org == null) throw new NoResultException();

        if (!org.getWorkers().isEmpty()) {
            Organization target = em.find(Organization.class, newOrgId);
            if (target == null) throw new IllegalArgumentException("Target organization not found");

            org.getWorkers().forEach(w -> {
                w.setOrganization(target);
                target.setEmployeesCount(target.getEmployeesCount() + 1);
            });
        }
        em.remove(org);
    }
}
