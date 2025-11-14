package com.is.is1.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Data
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @Valid
    private Address officialAddress;

    @Positive
    @Column(nullable = false)
    private double annualTurnover;

    @Positive
    @Column(nullable = false)
    private long employeesCount;               // поддерживается в сервисном слое

    @Size(max = 200)
    private String fullName;

    @Positive
    @Column(nullable = false)
    private float rating;

    // Бидерекционная связь (удобно, но можно убрать)
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    private Set<Worker> workers = new HashSet<>();
}
