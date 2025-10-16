package com.is.is1;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Worker {
    @Min(0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "coordinates_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "coordinates_y", nullable = false))
    })
    private Coordinates coordinates;
    @NotNull
    private LocalDateTime creationDate;
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "annualTurnover", column = @Column(name = "organization_annual_turnover", nullable = false)),
            @AttributeOverride(name = "employeesCount", column = @Column(name = "organization_employees_count", nullable = false)),
            @AttributeOverride(name = "fullName", column = @Column(name = "organization_full_name")),
            @AttributeOverride(name = "rating", column = @Column(name = "organization_rating", nullable = false)),
            @AttributeOverride(name = "officialAddress.street", column = @Column(name = "organization_address_street", nullable = false)),
            @AttributeOverride(name = "officialAddress.town.x", column = @Column(name = "organization_address_town_x", nullable = false)),
            @AttributeOverride(name = "officialAddress.town.y", column = @Column(name = "organization_address_town_y", nullable = false)),
            @AttributeOverride(name = "officialAddress.town.z", column = @Column(name = "organization_address_town_z", nullable = false)),
            @AttributeOverride(name = "officialAddress.town.name", column = @Column(name = "organization_address_town_name"))
    })
    private Organization organization;
    @NotNull
    @Min(0)
    private Float salary;
    @Min(0)
    private float rating;
    @NotNull
    private LocalDateTime startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private Position position;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "eyeColor", column = @Column(name = "person_eye_color", nullable = false)),
            @AttributeOverride(name = "hairColor", column = @Column(name = "person_hair_color", nullable = false)),
            @AttributeOverride(name = "height", column = @Column(name = "person_height")),
            @AttributeOverride(name = "passportID", column = @Column(name = "person_passport_id", unique = true)),
            @AttributeOverride(name = "nationality", column = @Column(name = "person_nationality")),
            @AttributeOverride(name = "location.x", column = @Column(name = "person_location_x")),
            @AttributeOverride(name = "location.y", column = @Column(name = "person_location_y")),
            @AttributeOverride(name = "location.z", column = @Column(name = "person_location_z")),
            @AttributeOverride(name = "location.name", column = @Column(name = "person_location_name"))
    })
    private Person person;
    @PrePersist
    public void prePersist(){
        creationDate = LocalDateTime.now();
    }
}
