package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import ru.itmo.model.Person;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import ru.itmo.repository.PersonRepository;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class PersonService {

    @Inject
    private PersonRepository personRepository;

    public Person createPerson(Person person) {
        return personRepository.create(person);
    }

    public List<Person> findAllPersonsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        return personRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
    }

    public Person updatePerson(Long id, Person person) {
        return personRepository.update(id, person);
    }

    public void deletePerson(Long id) {
        Person person = personRepository.findById(id);
        if (person == null) throw new NoResultException("Person not found");
        personRepository.delete(person);
    }

    public Person findPersonById(Long id) {
        return personRepository.findById(id);
    }

    public List<Person> findAllPersonsTruncated() {
        return personRepository.findAllTruncated();
    }
}
