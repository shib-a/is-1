package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.*;
import ru.itmo.model.Address;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.Data;
import ru.itmo.model.Coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class AddressService {
    @Inject
    private EntityManager entityManager;

    public List<Address> findAllAddressesPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Address> cq = cb.createQuery(Address.class);
        Root<Address> root = cq.from(Address.class);

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

        TypedQuery<Address> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Address findAddressById(Long id) {
        return entityManager.find(Address.class, id);
    }


    public Address createAddress(Address address) {
        entityManager.getTransaction().begin();
        entityManager.persist(address);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return address;
    }


    public Address updateAddress(Long id, Address address) {
        entityManager.getTransaction().begin();
        Address existing = entityManager.find(Address.class, id);
        if (existing == null) throw new NoResultException("Address not found");

        address.setId(id);
        var res = entityManager.merge(address);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return res;
    }

    public void deleteAddress(Long id) {
        entityManager.getTransaction().begin();
        Address address = entityManager.find(Address.class, id);
        if (address == null) throw new NoResultException("Address not found");

        entityManager.remove(address);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }
    public List<Address> findAllAddressesTruncated() {
        TypedQuery<Address> query = entityManager.createQuery("SELECT a FROM Address a ORDER BY a.id DESC", Address.class);
        query.setMaxResults(10);
        return query.getResultList();
    }
}
