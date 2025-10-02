package com.is.is1;


import jakarta.inject.Named;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Named
public class WorkerRepository {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("workerManagement");

}
