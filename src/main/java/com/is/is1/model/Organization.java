package com.is.is1.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Data
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Positive
    @Column(nullable = false)
    private double annualTurnover;

    @Positive
    private long employeesCount;

    @Size(max = 869)
    private String fullName;

    @Positive
    private float rating;

    @NotNull
    @ManyToOne
    @Valid
    @JoinColumn(name="location_id", nullable=false)
    private Address officialAddress;

    @OneToMany(mappedBy = "organizations")
    private List<Worker> workers;
}
