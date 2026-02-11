package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.NotFoundException;
import ru.itmo.DTO.WorkerDTO;
import ru.itmo.common.WorkerMapper;
import ru.itmo.config.CacheLogging;
import ru.itmo.model.Organization;
import ru.itmo.model.Person;
import ru.itmo.model.Worker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import lombok.Data;
import ru.itmo.repository.WorkerRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Data
@CacheLogging
public class WorkerService {

    @Inject
    private WorkerRepository workerRepository;

    @Inject
    private WorkerMapper mapper;

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

    public WorkerDTO updateWorker(Long id, WorkerDTO dto) {
        beginSerializableTransaction();
        try {
            Worker worker = workerRepository.findById(id);
            if (worker == null) throw new NotFoundException("Worker not found");
            mapper.updateFromDto(dto, worker);

            if (dto.getCoordinates() != null && dto.getCoordinates().getId() != null) {
                worker.setCoordinates(workerRepository.getCoordinatesReference(dto.getCoordinates().getId()));
            }

            if (dto.getOrganization() != null) {
                worker.setOrganization(dto.getOrganization().getId() != null ?
                        workerRepository.getOrganizationReference(dto.getOrganization().getId()) : null);
            }

            if (dto.getPerson() != null) {
                worker.setPerson(dto.getPerson().getId() != null ?
                        workerRepository.getPersonReference(dto.getPerson().getId()) : null);
            }

            Worker updated = workerRepository.update(worker);
            entityManager.getTransaction().commit();
            return mapper.toDto(updated);
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public WorkerDTO createWorker(WorkerDTO dto) {
        beginSerializableTransaction();
        try {
            Worker worker = mapper.toEntity(dto);
            System.out.println("Creating worker entity from DTO:" + worker.getName());

            if (dto.getCoordinates() != null && dto.getCoordinates().getId() != null) {
                worker.setCoordinates(workerRepository.getCoordinatesReference(dto.getCoordinates().getId()));
            }

            if (dto.getOrganization() != null && dto.getOrganization().getId() != null) {
                Organization org = workerRepository.getOrganizationReference(dto.getOrganization().getId());
                worker.setOrganization(org);
            }

            if (dto.getPerson() != null && dto.getPerson().getId() != null) {
                Person p = workerRepository.getPersonReference(dto.getPerson().getId());
                worker.setPerson(p);
            }

            Worker created = workerRepository.create(worker);
            entityManager.getTransaction().commit();
            return mapper.toDto(created);
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void deleteWorker(Long id) {
        beginSerializableTransaction();
        try {
            Worker worker = workerRepository.findById(id);
            if (worker == null) throw new NoResultException("Worker not found");
            workerRepository.delete(worker);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public WorkerDTO findWorkerById(Long id) {
        beginRepeatableReadTransaction();
        try {
            WorkerDTO result = mapper.toDto(workerRepository.findById(id));
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<WorkerDTO> findAllWorkersPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        beginRepeatableReadTransaction();
        try {
            List<Worker> results = workerRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
            List<WorkerDTO> result = mapper.toDtoList(results);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Map<Double, Long> groupBySalary() {
        beginRepeatableReadTransaction();
        try {
            Map<Double, Long> result = workerRepository.groupBySalary()
                    .stream()
                    .collect(Collectors.toMap(
                            arr -> (Double) arr[0],
                            arr -> (Long) arr[1]
                    ));
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public long countByEndDate(Date endDate) {
        beginRepeatableReadTransaction();
        try {
            long result;
            if (endDate == null) {
                result = workerRepository.countByEndDateNull();
            } else {
                result = workerRepository.countByEndDate(endDate);
            }
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Worker> findByNameContaining(String substring) {
        beginRepeatableReadTransaction();
        try {
            String pattern = "%" + substring.toLowerCase() + "%";
            List<Worker> result = workerRepository.findByNameContaining(pattern);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void indexSalaryForWorker(Long workerId, double coefficient) {
        beginSerializableTransaction();
        try {
            Worker worker = workerRepository.findById(workerId);
            if (worker == null) throw new NotFoundException("Worker not found");
            worker.setSalary(worker.getSalary() * coefficient);
            workerRepository.update(worker);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void indexSalaryForOrganization(Long organizationId, double coefficient) {
        beginSerializableTransaction();
        try {
            int updated = workerRepository.updateSalaryForOrganization(organizationId, coefficient);
            if (updated == 0) {
                throw new NotFoundException("No workers found in organization or organization not found");
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }
}
