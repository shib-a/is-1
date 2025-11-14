package com.is.is1.service;

import com.is.is1.model.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PersonService {
    @PersistenceContext(unitName = "workerManagement")
    private EntityManager em;

    public List<Person> findAll() {
        return em.createQuery("SELECT p FROM Person p", Person.class).getResultList();
    }

    public Person findById(Long id) {
        return em.find(Person.class, id);
    }

    @Transactional
    public Person create(Person p) {
        em.persist(p);
        return p;
    }

    @Transactional public Person update(Long id, Person p) {
        p.setId(id);
        return em.merge(p);
    }

    @Transactional public void delete(Long id) {
        Person p = em.find(Person.class, id);
        if (p != null) em.remove(p);
    }
}
