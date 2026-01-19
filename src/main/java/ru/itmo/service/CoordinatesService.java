package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import ru.itmo.model.Coordinates;
import jakarta.enterprise.context.ApplicationScoped;
import ru.itmo.repository.CoordinatesRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CoordinatesService {
    @Inject
    private CoordinatesRepository coordinatesRepository;

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

    public List<Coordinates> findAllCoordinatesPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        beginRepeatableReadTransaction();
        try {
            List<Coordinates> result = coordinatesRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Coordinates createCoordinates(Coordinates coordinates) {
        beginSerializableTransaction();
        try {
            Coordinates result = coordinatesRepository.create(coordinates);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Coordinates updateCoordinates(Long id, Coordinates coordinates) {
        beginSerializableTransaction();
        try {
            Coordinates result = coordinatesRepository.update(id, coordinates);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void deleteCoordinates(Long id) {
        beginSerializableTransaction();
        try {
            Coordinates coordinates = coordinatesRepository.findById(id);
            if (coordinates == null) throw new NoResultException("Coordinates not found");

            coordinatesRepository.nullifyWorkerReferences(id);
            coordinatesRepository.delete(coordinates);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Coordinates findCoordinatesById(Long id) {
        beginRepeatableReadTransaction();
        try {
            Coordinates result = coordinatesRepository.findById(id);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Coordinates> findAllCoordinatesTruncated() {
        beginRepeatableReadTransaction();
        try {
            List<Coordinates> result = coordinatesRepository.findAllTruncated();
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
