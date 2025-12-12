package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import ru.itmo.DTO.WorkerDTO;
import ru.itmo.common.WorkerMapper;
import ru.itmo.model.Organization;
import ru.itmo.model.Person;
import ru.itmo.model.Worker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import lombok.Data;
import ru.itmo.repository.WorkerRepository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Data
public class WorkerService {

    @Inject
    private WorkerRepository workerRepository;

    @Inject
    private WorkerMapper mapper;

    public WorkerDTO updateWorker(Long id, WorkerDTO dto) {
        Worker worker = workerRepository.findById(id);
        if (worker == null) throw new NotFoundException("Worker not found");
        mapper.updateFromDto(dto, worker);

        // Handle coordinates reference
        if (dto.getCoordinates() != null && dto.getCoordinates().getId() != null) {
            worker.setCoordinates(workerRepository.getCoordinatesReference(dto.getCoordinates().getId()));
        }

        // Handle organization reference
        if (dto.getOrganization() != null) {
            worker.setOrganization(dto.getOrganization().getId() != null ?
                    workerRepository.getOrganizationReference(dto.getOrganization().getId()) : null);
        }

        // Handle person reference (optional)
        if (dto.getPerson() != null) {
            worker.setPerson(dto.getPerson().getId() != null ?
                    workerRepository.getPersonReference(dto.getPerson().getId()) : null);
        }

        Worker updated = workerRepository.update(worker);
        return mapper.toDto(updated);
    }

    public WorkerDTO createWorker(WorkerDTO dto) {
        Worker worker = mapper.toEntity(dto);
        System.out.println("Creating worker entity from DTO:" + worker.getName());

        // Handle coordinates reference
        if (dto.getCoordinates() != null && dto.getCoordinates().getId() != null) {
            worker.setCoordinates(workerRepository.getCoordinatesReference(dto.getCoordinates().getId()));
        }

        // Handle organization reference
        if (dto.getOrganization() != null && dto.getOrganization().getId() != null) {
            Organization org = workerRepository.getOrganizationReference(dto.getOrganization().getId());
            worker.setOrganization(org);
        }

        // Handle person reference (optional)
        if (dto.getPerson() != null && dto.getPerson().getId() != null) {
            Person p = workerRepository.getPersonReference(dto.getPerson().getId());
            worker.setPerson(p);
        }

        Worker created = workerRepository.create(worker);
        return mapper.toDto(created);
    }

    public void deleteWorker(Long id) {
        Worker worker = workerRepository.findById(id);
        if (worker == null) throw new NoResultException("Worker not found");
        workerRepository.delete(worker);
    }

    public WorkerDTO findWorkerById(Long id) {
        return mapper.toDto(workerRepository.findById(id));
    }

    public List<WorkerDTO> findAllWorkersPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        List<Worker> results = workerRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
        return mapper.toDtoList(results);
    }

    public Map<Double, Long> groupBySalary() {
        return workerRepository.groupBySalary()
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Double) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    public long countByEndDate(Date endDate) {
        if (endDate == null) {
            return workerRepository.countByEndDateNull();
        }
        return workerRepository.countByEndDate(endDate);
    }

    public List<Worker> findByNameContaining(String substring) {
        String pattern = "%" + substring.toLowerCase() + "%";
        return workerRepository.findByNameContaining(pattern);
    }

    public void indexSalaryForWorker(Long workerId, double coefficient) {
        Worker worker = workerRepository.findById(workerId);
        if (worker == null) throw new NotFoundException("Worker not found");
        worker.setSalary(worker.getSalary() * coefficient);
        workerRepository.update(worker);
    }

    public void indexSalaryForOrganization(Long organizationId, double coefficient) {
        int updated = workerRepository.updateSalaryForOrganization(organizationId, coefficient);
        if (updated == 0) {
            throw new NotFoundException("No workers found in organization or organization not found");
        }
    }

}
