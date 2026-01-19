package ru.itmo.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import ru.itmo.model.Organization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OrganizationRepository {
    @Inject
    private EntityManager entityManager;

    public List<Organization> findAllPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Organization> cq = cb.createQuery(Organization.class);
        Root<Organization> root = cq.from(Organization.class);

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

        TypedQuery<Organization> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Organization create(Organization organization) {
        entityManager.persist(organization);
        entityManager.flush();
        return organization;
    }

    public Organization findById(Long id) {
        return entityManager.find(Organization.class, id);
    }

    public Organization findByIdOrNull(Long id) {
        return entityManager.find(Organization.class, id);
    }

    public void persistInTransaction(Organization organization) {
        entityManager.persist(organization);
    }

    public Organization update(Long id, Organization organization) {
        Organization existing = entityManager.find(Organization.class, id);
        if (existing == null) throw new NoResultException("Organization not found");

        organization.setId(id);
        var res = entityManager.merge(organization);
        entityManager.flush();
        return res;
    }

    public void delete(Organization organization) {
        entityManager.remove(organization);
        entityManager.flush();
    }

    public List<Organization> findAllTruncated() {
        TypedQuery<Organization> query = entityManager.createQuery("SELECT o FROM Organization o ORDER BY o.id DESC", Organization.class);
        query.setMaxResults(10);
        return query.getResultList();
    }

    public void deleteWithReassign(Organization org, Organization target) {
        if (!org.getWorkers().isEmpty()) {
            org.getWorkers().forEach(w -> {
                w.setOrganization(target);
                target.setEmployeesCount(target.getEmployeesCount() + 1);
            });
        }
        entityManager.remove(org);
    }
}
