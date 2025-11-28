package ru.itmo.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.itmo.model.Address;
import ru.itmo.model.Person;

import java.util.List;

@Data
public class LocationDTO {
    private Long id;
    private Integer x;
    private Integer y;
    private Integer z;
    private String name;
}
