package com.is.is1.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Entity
@Table(name = "persons")
@Data
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Color eyeColor;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Color hairColor;

    @ManyToOne
    private Location location;                     // может быть null

    @Positive
    @Column(nullable = false)
    private long height;

    @Column(unique = true)
    private String passportID;                     // уникальный, может быть null

    @Enumerated(EnumType.STRING)
    private Country nationality;                   // может быть null
}
