package com.is.is1.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="coordinates")
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "-573", inclusive = false)
    @Column(nullable = false)
    private Double x;

    @NotNull
    @DecimalMin(value = "-236", inclusive = false)
    @Column(nullable = false)
    private Double y;

    @OneToMany(mappedBy = "coordinates")
    private List<Worker> workers;
}
