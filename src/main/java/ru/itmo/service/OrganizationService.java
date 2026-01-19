package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import ru.itmo.model.Organization;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import lombok.Data;
import ru.itmo.repository.OrganizationRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class OrganizationService {
    @Inject
    private OrganizationRepository organizationRepository;

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

    public List<Organization> findAllOrganizationsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        beginRepeatableReadTransaction();
        try {
            List<Organization> result = organizationRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Organization createOrganization(Organization organization) {
        beginSerializableTransaction();
        try {
            Organization result = organizationRepository.create(organization);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Organization updateOrganization(Long id, Organization organization) {
        beginSerializableTransaction();
        try {
            Organization result = organizationRepository.update(id, organization);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void deleteOrganization(Long id) {
        beginSerializableTransaction();
        try {
            Organization organization = organizationRepository.findById(id);
            if (organization == null) throw new NoResultException("Organization not found");
            organizationRepository.delete(organization);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Organization findOrganizationById(Long id) {
        beginRepeatableReadTransaction();
        try {
            Organization result = organizationRepository.findById(id);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Organization> findAllOrganizationsTruncated() {
        beginRepeatableReadTransaction();
        try {
            List<Organization> result = organizationRepository.findAllTruncated();
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void deleteWithReassign(Long id, Long newOrgId) {
        beginSerializableTransaction();
        try {
            Organization org = organizationRepository.findById(id);
            if (org == null) throw new NoResultException();

            Organization target = organizationRepository.findById(newOrgId);
            if (target == null) throw new IllegalArgumentException("Target organization not found");

            organizationRepository.deleteWithReassign(org, target);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }
}
