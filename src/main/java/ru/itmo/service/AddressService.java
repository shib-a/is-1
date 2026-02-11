package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.*;
import ru.itmo.model.Address;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import ru.itmo.repository.AddressRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class AddressService {
    @Inject
    private AddressRepository addressRepository;

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

    public List<Address> findAllAddressesPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        beginRepeatableReadTransaction();
        try {
            List<Address> result = addressRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Address findAddressById(Long id) {
        beginRepeatableReadTransaction();
        try {
            Address result = addressRepository.findById(id);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Address createAddress(Address address) {
        beginSerializableTransaction();
        try {
            Address result = addressRepository.create(address);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Address updateAddress(Long id, Address address) {
        beginSerializableTransaction();
        try {
            Address result = addressRepository.update(id, address);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void deleteAddress(Long id) {
        beginSerializableTransaction();
        try {
            Address address = addressRepository.findById(id);
            if (address == null) throw new NoResultException("Address not found");

            long orgCount = entityManager.createQuery(
                "SELECT COUNT(o) FROM Organization o WHERE o.officialAddress.id = :addressId", Long.class)
                .setParameter("addressId", id)
                .getSingleResult();

            if (orgCount > 0) {
                throw new IllegalStateException(
                    "Cannot delete address: " + orgCount + " organization(s) are using this address. Please delete or reassign them first.");
            }

            addressRepository.delete(address);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Address> findAllAddressesTruncated() {
        beginRepeatableReadTransaction();
        try {
            List<Address> result = addressRepository.findAllTruncated();
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
