package ru.itmo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "organizations")
@Data
@Cacheable
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

    @OneToMany(mappedBy = "organization")
    @JsonIgnore
    private List<Worker> workers;
}
