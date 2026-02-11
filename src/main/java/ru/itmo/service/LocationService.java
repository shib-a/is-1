package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import ru.itmo.model.Location;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import lombok.Data;
import ru.itmo.repository.LocationRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class LocationService {
    @Inject
    private LocationRepository locationRepository;

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

    public List<Location> findAllLocationsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        beginRepeatableReadTransaction();
        try {
            List<Location> result = locationRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Location findLocationById(Long id) {
        beginRepeatableReadTransaction();
        try {
            Location result = locationRepository.findById(id);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Location createLocation(Location location) {
        beginSerializableTransaction();
        try {
            Location result = locationRepository.create(location);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Location updateLocation(Long id, Location location) {
        beginSerializableTransaction();
        try {
            Location result = locationRepository.update(id, location);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void deleteLocation(Long id) {
        beginSerializableTransaction();
        try {
            Location location = locationRepository.findById(id);
            if (location == null) throw new NoResultException("Location not found");

            long addressCount = entityManager.createQuery(
                "SELECT COUNT(a) FROM Address a WHERE a.town.id = :locId", Long.class)
                .setParameter("locId", id)
                .getSingleResult();

            if (addressCount > 0) {
                throw new IllegalStateException(
                    "Cannot delete location: " + addressCount + " address(es) are using this location. Please delete or reassign them first.");
            }

            long personCount = entityManager.createQuery(
                "SELECT COUNT(p) FROM Person p WHERE p.location.id = :locId", Long.class)
                .setParameter("locId", id)
                .getSingleResult();

            if (personCount > 0) {
                throw new IllegalStateException(
                    "Cannot delete location: " + personCount + " person(s) are using this location. Please delete or reassign them first.");
            }

            locationRepository.delete(location);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Location> findAllLocationsTruncated() {
        beginRepeatableReadTransaction();
        try {
            List<Location> result = locationRepository.findAllTruncated();
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
