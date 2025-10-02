package com.is.is1;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
@ApplicationScoped
public class WorkerService {
    @PersistenceContext(unitName = "workerManagement")
    private EntityManager em;
    public void save(Worker worker){
        em.persist(worker);
    }
    public Worker find(Long id){
        return em.find(Worker.class, id);
    }
    public List<Worker> findAll(){
        return em.createQuery("select w from Worker w", Worker.class).getResultList();
    }
    public List<Worker> findAllByNameContainsSubstr(String substr){
        TypedQuery<Worker> query = em.createQuery("select w from Worker w where w.name like :substr", Worker.class);
        query.setParameter("substr", "%"+substr+"%");
        return query.getResultList();
    }
    public List<Worker> findAllByNameStartsWithSubstr(String substr){
        TypedQuery<Worker> query = em.createQuery("select w from Worker w where w.name like :substr", Worker.class);
        query.setParameter("substr", substr+"%");
        return query.getResultList();
    }
    public List<Worker> findAllByRatingLowerThan(float rating){
        TypedQuery<Worker> query = em.createQuery("select w from Worker w where w.rating < :value", Worker.class);
        query.setParameter("value", rating);
        return query.getResultList();
    }
}
