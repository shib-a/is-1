package ru.itmo.service;

import jakarta.inject.Inject;
import ru.itmo.model.Address;
import ru.itmo.model.Organization;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.Data;
import ru.itmo.model.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class OrganizationService {
    @Inject
    private EntityManager entityManager;

    public Organization createOrganization(Organization organization) {
        entityManager.getTransaction().begin();
        entityManager.persist(organization);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return organization;
    }

    public Organization updateOrganization(Long id, Organization organization) {
        Organization existing = entityManager.find(Organization.class, id);
        if (existing == null) throw new NoResultException("Person not found");

        entityManager.getTransaction().begin();
        organization.setId(id);
        var res = entityManager.merge(organization);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return res;
    }

    public void deleteOrganization(Long id) {
        Organization organization = entityManager.find(Organization.class, id);
        if (organization == null) throw new NoResultException("Address not found");

        entityManager.getTransaction().begin();
        entityManager.remove(organization);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    public Organization findOrganizationById(Long id) {
        return entityManager.find(Organization.class, id);
    }

    private List<Organization> findAllOrganizationsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
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
        TypedQuery<Organization> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        List<Organization> results = query.getResultList();
        return results;
    }

    public List<Organization> findAllOrganizationsTruncated() {
        TypedQuery<Organization> query = entityManager.createQuery("SELECT o FROM Organization o ORDER BY o.id DESC", Organization.class);
        query.setMaxResults(10);
        return query.getResultList();
    }

    @Transactional
    public void deleteWithReassign(Long id, Long newOrgId) {
        Organization org = entityManager.find(Organization.class, id);
        if (org == null) throw new NoResultException();

        if (!org.getWorkers().isEmpty()) {
            Organization target = entityManager.find(Organization.class, newOrgId);
            if (target == null) throw new IllegalArgumentException("Target organization not found");

            org.getWorkers().forEach(w -> {
                w.setOrganization(target);
                target.setEmployeesCount(target.getEmployeesCount() + 1);
            });
        }
        entityManager.remove(org);
    }
}
