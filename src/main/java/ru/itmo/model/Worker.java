package ru.itmo.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "workers")
@Data
public class Worker {
    @Id
//    @Min(1)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @NotNull
    @Column(nullable = false)
    private String name;


    @Column(nullable = false, updatable = false)
    private LocalDate creationDate;

    @Positive
    @Column(nullable = false)
    private double salary;

    @NotNull(message = "Rating cannot be null")
    @Positive(message = "Rating must be > 0")
    @Column(nullable = false)
    private Double rating;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Enumerated(EnumType.STRING)
    private Position position;

    @ManyToOne
    @Valid
    @NotNull
    @JoinColumn(nullable = false, name = "coordinates_id")
    private Coordinates coordinates;

    @Valid
    @ManyToOne(optional = false,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Valid
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @PrePersist
    private void onCreate() {
        if (creationDate == null) {
            creationDate = LocalDate.now();
        }
    }
}
