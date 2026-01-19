package ru.itmo.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import ru.itmo.model.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AddressRepository {
    @Inject
    private EntityManager entityManager;

    public List<Address> findAllPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Address> cq = cb.createQuery(Address.class);
        Root<Address> root = cq.from(Address.class);

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

        TypedQuery<Address> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Address findById(Long id) {
        return entityManager.find(Address.class, id);
    }

    public Address findByIdOrNull(Long id) {
        return entityManager.find(Address.class, id);
    }

    public void persistInTransaction(Address address) {
        entityManager.persist(address);
    }

    public Address create(Address address) {
        entityManager.persist(address);
        entityManager.flush();
        return address;
    }

    public Address update(Long id, Address address) {
        Address existing = entityManager.find(Address.class, id);
        if (existing == null) throw new NoResultException("Address not found");

        address.setId(id);
        var res = entityManager.merge(address);
        entityManager.flush();
        return res;
    }

    public void delete(Address address) {
        entityManager.remove(address);
        entityManager.flush();
    }

    public void nullifyOrganizationReferences(Long addressId) {
        entityManager.createQuery("UPDATE Organization o SET o.officialAddress = NULL WHERE o.officialAddress.id = :addressId")
                .setParameter("addressId", addressId)
                .executeUpdate();
        entityManager.flush();
    }

    public List<Address> findAllTruncated() {
        TypedQuery<Address> query = entityManager.createQuery("SELECT a FROM Address a ORDER BY a.id DESC", Address.class);
        query.setMaxResults(10);
        return query.getResultList();
    }
}
