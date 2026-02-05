package ru.itmo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "persons")
@Data
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Color eyeColor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Color hairColor;

    @Positive
    @Column(nullable = false)
    private long height;

    @Column
    private String passportID;

    @Enumerated(EnumType.STRING)
    private Country nationality;

    @OneToMany(mappedBy = "person")
    @JsonIgnore
    private List<Worker> workers;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
}
