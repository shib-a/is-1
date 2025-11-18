package com.is.is1.service;

import com.is.is1.model.Organization;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Data;

import java.util.List;

@ApplicationScoped
@Data
public class OrganizationService {
    @PersistenceContext(unitName = "workerManagement")
    private EntityManager em;

    public List<Organization> findAll() {
        return em.createQuery("SELECT o FROM Organization o", Organization.class)
                .getResultList();
    }
    public Organization findById(Long id) { return em.find(Organization.class, id); }

    @Transactional
    public Organization create(Organization o) {
        o.setEmployeesCount(0);
        em.persist(o);
        return o;
    }

    @Transactional
    public Organization update(Long id, Organization o) {
        o.setId(id);
        return em.merge(o);
    }

    @Transactional
    public void deleteWithReassign(Long id, Long newOrgId) {
        Organization org = em.find(Organization.class, id);
        if (org == null) throw new NoResultException();

        if (!org.getWorkers().isEmpty()) {
            Organization target = em.find(Organization.class, newOrgId);
            if (target == null) throw new IllegalArgumentException("Target organization not found");

            org.getWorkers().forEach(w -> {
                w.setOrganization(target);
                target.setEmployeesCount(target.getEmployeesCount() + 1);
            });
        }
        em.remove(org);
    }
}
