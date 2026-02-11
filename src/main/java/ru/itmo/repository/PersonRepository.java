package ru.itmo.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import ru.itmo.model.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PersonRepository {
    @Inject
    private EntityManager entityManager;

    public List<Person> findAllPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> cq = cb.createQuery(Person.class);
        Root<Person> root = cq.from(Person.class);

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

        TypedQuery<Person> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Person create(Person person) {
        entityManager.persist(person);
        entityManager.flush();
        return person;
    }

    public Person findById(Long id) {
        return entityManager.find(Person.class, id);
    }

    public Person findByIdOrNull(Long id) {
        return entityManager.find(Person.class, id);
    }

    public void persistInTransaction(Person person) {
        entityManager.persist(person);
    }

    public Person update(Long id, Person person) {
        Person existing = entityManager.find(Person.class, id);
        if (existing == null) throw new NoResultException("Person not found");

        person.setId(id);
        var res = entityManager.merge(person);
        entityManager.flush();
        return res;
    }

    public void delete(Person person) {
        entityManager.remove(person);
        entityManager.flush();
    }

    public void nullifyWorkerReferences(Long personId) {
        entityManager.createQuery("UPDATE Worker w SET w.person = NULL WHERE w.person.id = :personId")
                .setParameter("personId", personId)
                .executeUpdate();
        entityManager.flush();
    }

    public List<Person> findAllTruncated() {
        TypedQuery<Person> query = entityManager.createQuery("SELECT p FROM Person p ORDER BY p.id DESC", Person.class);
        query.setMaxResults(10);
        return query.getResultList();
    }

    public boolean existsByPassportID(String passportID) {
        if (passportID == null) return false;
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(p) FROM Person p WHERE p.passportID = :passportID", Long.class);
        query.setParameter("passportID", passportID);
        return query.getSingleResult() > 0;
    }

    public boolean existsByPassportIDExcludingId(String passportID, Long excludeId) {
        if (passportID == null) return false;
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(p) FROM Person p WHERE p.passportID = :passportID AND p.id <> :excludeId", Long.class);
        query.setParameter("passportID", passportID);
        query.setParameter("excludeId", excludeId);
        return query.getSingleResult() > 0;
    }

    public boolean existsByPassportIDWithLock(String passportID) {
        if (passportID == null) return false;

        TypedQuery<Person> query = entityManager.createQuery(
                "SELECT p FROM Person p WHERE p.passportID = :passportID", Person.class);
        query.setParameter("passportID", passportID);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        List<Person> results = query.getResultList();
        return !results.isEmpty();
    }

    public boolean existsByPassportIDExcludingIdWithLock(String passportID, Long excludeId) {
        if (passportID == null) return false;

        TypedQuery<Person> query = entityManager.createQuery(
                "SELECT p FROM Person p WHERE p.passportID = :passportID AND p.id <> :excludeId", Person.class);
        query.setParameter("passportID", passportID);
        query.setParameter("excludeId", excludeId);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        List<Person> results = query.getResultList();
        return !results.isEmpty();
    }
}
