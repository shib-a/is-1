package ru.itmo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import ru.itmo.DTO.ImportHistoryDTO;
import ru.itmo.DTO.WorkerDTO;
import ru.itmo.config.CacheLogging;
import ru.itmo.config.MinioService;
import ru.itmo.model.*;
import ru.itmo.repository.*;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@ApplicationScoped
public class ImportService {

    private static final Logger LOGGER = Logger.getLogger(ImportService.class.getName());

    @Inject
    private WorkerRepository workerRepository;

    @Inject
    private CoordinatesRepository coordinatesRepository;

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private AddressRepository addressRepository;

    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private PersonRepository personRepository;

    @Inject
    private ImportHistoryRepository importHistoryRepository;

    @Inject
    private EntityManager entityManager;

    @Inject
    private Validator validator;

    @Inject
    private MinioService minioService;

    private Set<String> createdPassportIDs;

    @CacheLogging
    public ImportHistoryDTO importWorkers(List<WorkerDTO> workerDTOs, byte[] fileContent) {
        ImportHistory history = new ImportHistory();
        history.setStatus(ImportStatus.FAILED);
        createdPassportIDs = new HashSet<>();

        String minioFileName = null;
        boolean minioUploadSuccess = false;
        boolean dbTransactionStarted = false;

        try {
            LOGGER.info("Two-Phase Commit: Starting PREPARE phase");

            entityManager.getTransaction().begin();
            dbTransactionStarted = true;
            try {
                entityManager.unwrap(Connection.class).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            } catch (Exception ignored) {}

            List<Worker> workers = new ArrayList<>();
            validateAndPrepareWorkers(workerDTOs, workers);

            for (Worker worker : workers) {
                workerRepository.persistWorker(worker);
            }

            if (fileContent != null && fileContent.length > 0) {
                minioFileName = generateFileName();
                try {
                    minioService.uploadFile(minioFileName, fileContent);
                    minioUploadSuccess = true;
                    LOGGER.info("Two-Phase Commit: File uploaded to MinIO: " + minioFileName);
                } catch (MinioService.MinioException e) {
                    LOGGER.warning("Two-Phase Commit: MinIO upload failed: " + e.getMessage());
                    throw new TwoPhaseCommitException("MinIO upload failed: " + e.getMessage(), e);
                }
            }

            LOGGER.info("Two-Phase Commit: Starting COMMIT phase");

            entityManager.getTransaction().commit();
            dbTransactionStarted = false;
            LOGGER.info("Two-Phase Commit: Database transaction committed");

            history.setStatus(ImportStatus.SUCCESS);
            history.setAddedCount(workers.size());
            history.setFileName(minioFileName);

            LOGGER.info("Two-Phase Commit: COMMIT phase completed successfully");
        } catch (Exception e) {
            LOGGER.warning("Two-Phase Commit: Error occurred, starting ROLLBACK: " + e.getMessage());

            if (dbTransactionStarted && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
                LOGGER.info("Two-Phase Commit: Database transaction rolled back");
            }

            if (minioUploadSuccess && minioFileName != null) {
                try {
                    minioService.deleteFile(minioFileName);
                    LOGGER.info("Two-Phase Commit: MinIO file deleted (rollback): " + minioFileName);
                } catch (MinioService.MinioException me) {
                    LOGGER.severe("Two-Phase Commit: Failed to rollback MinIO file: " + me.getMessage());
                }
            }

            history.setStatus(ImportStatus.FAILED);
            history.setErrorMessage(e.getMessage());
        }

        return saveImportHistory(history);
    }

    private void validateAndPrepareWorkers(List<WorkerDTO> workerDTOs, List<Worker> workers) throws ValidationException {
        for (int i = 0; i < workerDTOs.size(); i++) {
            WorkerDTO dto = workerDTOs.get(i);

            Worker worker = new Worker();
            worker.setName(dto.getName());
            worker.setSalary(dto.getSalary());
            worker.setRating(dto.getRating());
            worker.setStartDate(dto.getStartDate());
            worker.setEndDate(dto.getEndDate());
            worker.setPosition(dto.getPosition());

            if (dto.getCoordinates() != null) {
                Coordinates coordinates = processCoordinates(dto.getCoordinates());
                worker.setCoordinates(coordinates);
            } else {
                throw new ValidationException("Worker at index " + i + ": coordinates are required");
            }

            if (dto.getOrganization() != null) {
                Organization organization = processOrganization(dto.getOrganization());
                worker.setOrganization(organization);
            } else {
                throw new ValidationException("Worker at index " + i + ": organization is required");
            }

            if (dto.getPerson() != null) {
                Person person = processPerson(dto.getPerson());
                worker.setPerson(person);
            }

            Set<ConstraintViolation<Worker>> violations = validator.validate(worker);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                throw new ValidationException("Worker at index " + i + " validation failed: " + errors);
            }

            workers.add(worker);
        }
    }

    private String generateFileName() {
        return "import-" + UUID.randomUUID() + ".json";
    }

    private ImportHistoryDTO saveImportHistory(ImportHistory history) {
        entityManager.getTransaction().begin();
        try {
            ImportHistory savedHistory = importHistoryRepository.create(history);
            entityManager.getTransaction().commit();
            return mapToDTO(savedHistory);
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public byte[] downloadImportFile(String fileName) throws MinioService.MinioException {
        return minioService.downloadFile(fileName);
    }

    private Coordinates processCoordinates(ru.itmo.DTO.CoordinatesDTO dto) throws ValidationException {
        if (dto.getId() != null) {
            Coordinates existing = coordinatesRepository.findByIdOrNull(dto.getId());
            if (existing == null) {
                throw new ValidationException("Coordinates with id " + dto.getId() + " not found");
            }
            return existing;
        } else if (dto.getX() != null && dto.getY() != null) {
            Coordinates coordinates = new Coordinates();
            coordinates.setX(dto.getX());
            coordinates.setY(dto.getY());

            Set<ConstraintViolation<Coordinates>> violations = validator.validate(coordinates);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                throw new ValidationException("Coordinates validation failed: " + errors);
            }

            coordinatesRepository.persistInTransaction(coordinates);
            return coordinates;
        } else {
            throw new ValidationException("Coordinates must have either id or both x and y values");
        }
    }

    private Location processLocation(ru.itmo.DTO.LocationDTO dto) throws ValidationException {
        if (dto.getId() != null) {
            Location existing = locationRepository.findByIdOrNull(dto.getId());
            if (existing == null) {
                throw new ValidationException("Location with id " + dto.getId() + " not found");
            }
            return existing;
        } else {
            Location location = new Location();
            location.setX(dto.getX());
            location.setY(dto.getY());
            location.setZ(dto.getZ());
            location.setName(dto.getName());

            Set<ConstraintViolation<Location>> violations = validator.validate(location);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                throw new ValidationException("Location validation failed: " + errors);
            }

            locationRepository.persistInTransaction(location);
            return location;
        }
    }

    private Address processAddress(ru.itmo.DTO.AddressDTO dto) throws ValidationException {
        if (dto.getId() != null) {
            Address existing = addressRepository.findByIdOrNull(dto.getId());
            if (existing == null) {
                throw new ValidationException("Address with id " + dto.getId() + " not found");
            }
            return existing;
        } else {
            Address address = new Address();
            address.setStreet(dto.getStreet());

            if (dto.getTown() != null) {
                Location location = processLocation(dto.getTown());
                address.setTown(location);
            } else {
                throw new ValidationException("Address must have a town");
            }

            Set<ConstraintViolation<Address>> violations = validator.validate(address);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                throw new ValidationException("Address validation failed: " + errors);
            }

            addressRepository.persistInTransaction(address);
            return address;
        }
    }

    private Organization processOrganization(ru.itmo.DTO.OrganizationDTO dto) throws ValidationException {
        if (dto.getId() != null) {
            Organization existing = organizationRepository.findByIdOrNull(dto.getId());
            if (existing == null) {
                throw new ValidationException("Organization with id " + dto.getId() + " not found");
            }
            return existing;
        } else {
            Organization organization = new Organization();
            organization.setAnnualTurnover(dto.getAnnualTurnover());
            organization.setEmployeesCount(dto.getEmployeesCount());
            organization.setFullName(dto.getFullName());
            organization.setRating(dto.getRating());

            if (dto.getOfficialAddress() != null) {
                Address address = processAddress(dto.getOfficialAddress());
                organization.setOfficialAddress(address);
            } else {
                throw new ValidationException("Organization must have an official address");
            }

            Set<ConstraintViolation<Organization>> violations = validator.validate(organization);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                throw new ValidationException("Organization validation failed: " + errors);
            }

            organizationRepository.persistInTransaction(organization);
            return organization;
        }
    }

    private Person processPerson(ru.itmo.DTO.PersonDTO dto) throws ValidationException {
        if (dto.getId() != null) {
            Person existing = personRepository.findByIdOrNull(dto.getId());
            if (existing == null) {
                throw new ValidationException("Person with id " + dto.getId() + " not found");
            }
            return existing;
        } else {
            Person person = new Person();
            person.setEyeColor(dto.getEyeColor());
            person.setHairColor(dto.getHairColor());
            person.setHeight(dto.getHeight());
            person.setPassportID(dto.getPassportID());
            person.setNationality(dto.getNationality());


            if (person.getPassportID() != null) {
                if (createdPassportIDs.contains(person.getPassportID())) {
                    throw new ValidationException("Person with passportID '" + person.getPassportID() + "' already exists in this import");
                }

                if (personRepository.existsByPassportIDWithLock(person.getPassportID())) {
                    throw new ValidationException("Person with passportID '" + person.getPassportID() + "' already exists in database");
                }

                createdPassportIDs.add(person.getPassportID());
            }

            if (dto.getLocation() != null) {
                Location location = processLocation(dto.getLocation());
                person.setLocation(location);
            }

            Set<ConstraintViolation<Person>> violations = validator.validate(person);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                throw new ValidationException("Person validation failed: " + errors);
            }

            personRepository.persistInTransaction(person);
            return person;
        }
    }

    private ImportHistoryDTO mapToDTO(ImportHistory history) {
        ImportHistoryDTO dto = new ImportHistoryDTO();
        dto.setId(history.getId());
        dto.setTimestamp(history.getTimestamp());
        dto.setStatus(history.getStatus());
        dto.setAddedCount(history.getAddedCount());
        dto.setErrorMessage(history.getErrorMessage());
        dto.setFileName(history.getFileName());
        return dto;
    }

    private void beginRepeatableReadTransaction() {
        entityManager.getTransaction().begin();
        try {
            entityManager.unwrap(Connection.class).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        } catch (Exception ignored) {}
    }

    @CacheLogging
    public List<ImportHistoryDTO> getImportHistory(int page, int pageSize, String sortField, String sortDirection) {
        beginRepeatableReadTransaction();
        try {
            List<ImportHistory> histories = importHistoryRepository.findAllPaged(page, pageSize, sortField, sortDirection);
            List<ImportHistoryDTO> result = histories.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public long getImportHistoryCount() {
        beginRepeatableReadTransaction();
        try {
            long result = importHistoryRepository.count();
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static class TwoPhaseCommitException extends Exception {
        public TwoPhaseCommitException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

