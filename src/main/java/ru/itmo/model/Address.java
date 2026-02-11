package ru.itmo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "addresses")
@Cacheable
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String street;

    @NotNull
    @Valid
    @ManyToOne
    @JoinColumn(name="town_id", nullable = false)
    private Location town;

    @OneToMany(mappedBy = "officialAddress")
    @JsonIgnore
    @Valid
    private List<Organization> organizations;
}
