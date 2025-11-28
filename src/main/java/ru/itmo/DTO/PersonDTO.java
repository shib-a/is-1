package ru.itmo.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.itmo.model.Color;
import ru.itmo.model.Country;
import ru.itmo.model.Location;
import ru.itmo.model.Worker;

import java.util.List;

@Data
public class PersonDTO {
    private Long id;
    private Color eyeColor;
    private Color hairColor;
    private long height;
    private String passportID;
    private Country nationality;
    private LocationDTO location;
}
