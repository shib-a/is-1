package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import ru.itmo.model.Address;
import ru.itmo.model.Coordinates;
import ru.itmo.model.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class PersonService {

    @Inject
    private EntityManager entityManager;

    public Person createPerson(Person person) {
        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return person;
    }

    public Person updatePerson(Long id, Person person) {
        Person existing = entityManager.find(Person.class, id);
        if (existing == null) throw new NoResultException("Person not found");

        entityManager.getTransaction().begin();
        person.setId(id);
        var res = entityManager.merge(person);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return res;
    }

    public void deletePerson(Long id) {
        Person person = entityManager.find(Person.class, id);
        if (person == null) throw new NoResultException("Address not found");

        entityManager.getTransaction().begin();
        entityManager.remove(person);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    public Person findPersonById(Long id) {
        return entityManager.find(Person.class, id);
    }

    private List<Person> findAllPersonsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> cq = cb.createQuery(Person.class);
        Root<Person> root = cq.from(Person.class);

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
        TypedQuery<Person> query = entityManager.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        List<Person> results = query.getResultList();
        return results;
    }
    public List<Person> findAllPersonsTruncated() {
        TypedQuery<Person> query = entityManager.createQuery("SELECT p FROM Person p ORDER BY p.id DESC", Person.class);
        query.setMaxResults(10);
        return query.getResultList();
    }
}
