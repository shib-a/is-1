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

        TypedQuery<Person> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Person create(Person person) {
        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return person;
    }

    public Person findById(Long id) {
        return entityManager.find(Person.class, id);
    }

    public Person update(Long id, Person person) {
        Person existing = entityManager.find(Person.class, id);
        if (existing == null) throw new NoResultException("Person not found");

        entityManager.getTransaction().begin();
        person.setId(id);
        var res = entityManager.merge(person);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return res;
    }

    public void delete(Person person) {
        entityManager.getTransaction().begin();
        entityManager.remove(person);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    public List<Person> findAllTruncated() {
        TypedQuery<Person> query = entityManager.createQuery("SELECT p FROM Person p ORDER BY p.id DESC", Person.class);
        query.setMaxResults(10);
        return query.getResultList();
    }
}
