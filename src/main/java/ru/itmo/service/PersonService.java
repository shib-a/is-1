package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import ru.itmo.model.Person;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import ru.itmo.repository.PersonRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class PersonService {

    @Inject
    private PersonRepository personRepository;

    @Inject
    private EntityManager entityManager;

    private void beginSerializableTransaction() {
        entityManager.getTransaction().begin();
        try {
            entityManager.unwrap(Connection.class).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        } catch (SQLException ignored) {}
    }

    private void beginRepeatableReadTransaction() {
        entityManager.getTransaction().begin();
        try {
            entityManager.unwrap(Connection.class).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        } catch (SQLException ignored) {}
    }

    public Person createPerson(Person person) {
        beginSerializableTransaction();
        try {
            if (person.getPassportID() != null &&
                personRepository.existsByPassportIDWithLock(person.getPassportID())) {
                throw new IllegalArgumentException(
                    "Person with passportID '" + person.getPassportID() + "' already exists");
            }

            Person result = personRepository.create(person);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Person> findAllPersonsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        beginRepeatableReadTransaction();
        try {
            List<Person> result = personRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Person updatePerson(Long id, Person person) {
        beginSerializableTransaction();
        try {
            if (person.getPassportID() != null &&
                personRepository.existsByPassportIDExcludingIdWithLock(person.getPassportID(), id)) {
                throw new IllegalArgumentException(
                    "Person with passportID '" + person.getPassportID() + "' already exists");
            }

            Person result = personRepository.update(id, person);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void deletePerson(Long id) {
        beginSerializableTransaction();
        try {
            Person person = personRepository.findById(id);
            if (person == null) throw new NoResultException("Person not found");

            personRepository.nullifyWorkerReferences(id);
            personRepository.delete(person);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Person findPersonById(Long id) {
        beginRepeatableReadTransaction();
        try {
            Person result = personRepository.findById(id);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Person> findAllPersonsTruncated() {
        beginRepeatableReadTransaction();
        try {
            List<Person> result = personRepository.findAllTruncated();
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }
}
