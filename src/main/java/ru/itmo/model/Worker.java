package ru.itmo.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "workers")
@Data
@Getter @Setter
@Cacheable
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @NotNull
    @JoinColumn(nullable = false, name = "coordinates_id")
    private Coordinates coordinates;

    @ManyToOne(optional = false,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @PrePersist
    private void onCreate() {
        if (creationDate == null) {
            creationDate = LocalDate.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public LocalDate getCreationDate() {
        return creationDate;
    }
}
