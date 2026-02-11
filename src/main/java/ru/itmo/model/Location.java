package ru.itmo.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name="locations")
@Cacheable
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int x;

    @NotNull
    @Column(nullable = false)
    private Integer y;

    private int z;

    private String name;

    @OneToMany(mappedBy = "location")
    @JsonIgnore
    private List<Person> persons;

    @OneToMany(mappedBy = "town")
    @JsonIgnore
    private List<Address> addresses;
}
