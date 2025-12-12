package ru.itmo.common;

import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.itmo.model.*;
import ru.itmo.DTO.*;

import java.util.List;

@ApplicationScoped
@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkerMapper {

    WorkerMapper INSTANCE = Mappers.getMapper(WorkerMapper.class);

    WorkerDTO toDto(Worker worker);
    List<WorkerDTO> toDtoList(List<Worker> workers);

    OrganizationDTO organizationToDto(Organization org);
    PersonDTO personToDto(Person person);
    CoordinatesDTO coordinatesToDto(Coordinates coordinates);
    AddressDTO addressToDto(Address address);
    LocationDTO locationToDto(Location location);

    // Map simple fields, ignore entity references (will be set manually in service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "coordinates", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "person", ignore = true)
    Worker toEntity(WorkerDTO dto);

    // Update simple fields, ignore entity references and IDs
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "coordinates", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "person", ignore = true)
    void updateFromDto(WorkerDTO dto, @MappingTarget Worker worker);
}