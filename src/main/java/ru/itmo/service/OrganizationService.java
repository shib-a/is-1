package ru.itmo.service;

import jakarta.inject.Inject;
import ru.itmo.model.Organization;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import lombok.Data;
import ru.itmo.repository.OrganizationRepository;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class OrganizationService {
    @Inject
    private OrganizationRepository organizationRepository;

    public List<Organization> findAllOrganizationsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        return organizationRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
    }

    public Organization createOrganization(Organization organization) {
        return organizationRepository.create(organization);
    }

    public Organization updateOrganization(Long id, Organization organization) {
        return organizationRepository.update(id, organization);
    }

    public void deleteOrganization(Long id) {
        Organization organization = organizationRepository.findById(id);
        if (organization == null) throw new NoResultException("Organization not found");
        organizationRepository.delete(organization);
    }

    public Organization findOrganizationById(Long id) {
        return organizationRepository.findById(id);
    }

    public List<Organization> findAllOrganizationsTruncated() {
        return organizationRepository.findAllTruncated();
    }

    @Transactional
    public void deleteWithReassign(Long id, Long newOrgId) {
        Organization org = organizationRepository.findById(id);
        if (org == null) throw new NoResultException();

        Organization target = organizationRepository.findById(newOrgId);
        if (target == null) throw new IllegalArgumentException("Target organization not found");

        organizationRepository.deleteWithReassign(org, target);
    }
}
