package ru.itmo.DTO;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.itmo.model.Location;

@Data
public class AddressDTO {
    private Long id;
    private String street;
    private LocationDTO town;
}
