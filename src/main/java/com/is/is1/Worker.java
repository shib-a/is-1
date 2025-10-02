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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"passport_id"}))
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
    private Coordinates coordinates;
    @NotNull
    private LocalDateTime creationDate;
    @NotNull
    @Embedded
    private Organization organization;
    @NotNull
    @Min(0)
    private Float salary;
    @Min(0)
    private float rating;
    @NotNull
    private LocalDateTime startDate;
    private LocalDate endDate;
    private Position position;
    @Embedded
    private Person person;
    @PrePersist
    public void prePersist(){
        creationDate = LocalDateTime.now();
    }
}
