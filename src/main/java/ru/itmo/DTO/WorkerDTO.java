package ru.itmo.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.model.Position;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class WorkerDTO {
    private Long id;
    private String name;
    private CoordinatesDTO coordinates;
    private LocalDate creationDate;
    private OrganizationDTO organization;
    private Float salary;
    private Double rating;
    private LocalDate startDate;
    private Date endDate;
    private Position position;
    private PersonDTO person;
}