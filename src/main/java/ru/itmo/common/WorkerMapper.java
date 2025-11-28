package ru.itmo.common;

import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.itmo.model.*;
import ru.itmo.DTO.*;

import java.util.List;

@ApplicationScoped
@Mapper(componentModel = "cdi")
public interface WorkerMapper {

    WorkerMapper INSTANCE = Mappers.getMapper(WorkerMapper.class);

    WorkerDTO toDto(Worker worker);
    List<WorkerDTO> toDtoList(List<Worker> workers);

    OrganizationDTO organizationToDto(Organization org);
    PersonDTO personToDto(Person person);
    CoordinatesDTO coordinatesToDto(Coordinates coordinates);
    AddressDTO addressToDto(Address address);
    LocationDTO locationToDto(Location location);


    Worker toEntity(WorkerDTO dto);

    void updateFromDto(WorkerDTO dto, @MappingTarget Worker worker);
}