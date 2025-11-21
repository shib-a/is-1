package ru.itmo.service;

import ru.itmo.model.Address;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class AddressService {
    @PersistenceContext(unitName = "workerManagement")
    private EntityManager entityManager;

    private List<Address> findAllAddressesPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Address> cq = cb.createQuery(Address.class);
        Root<Address> root = cq.from(Address.class);

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
        TypedQuery<Address> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Address findAddressById(Long id) {
        return entityManager.find(Address.class, id);
    }

    @Transactional
    public Address createAddress(Address address) {
        entityManager.persist(address);
        return address;
    }

    @Transactional
    public Address updateAddress(Long id, Address address) {
        Address existing = entityManager.find(Address.class, id);
        if (existing == null) throw new NoResultException("Address not found");

        address.setId(id);
        return entityManager.merge(address);
    }

    @Transactional
    public void deleteAddress(Long id) {
        Address address = entityManager.find(Address.class, id);
        if (address == null) throw new NoResultException("Address not found");

        entityManager.remove(address);
    }
}
