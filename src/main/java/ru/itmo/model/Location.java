package ru.itmo.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name="locations")
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

    @OneToMany(mappedBy = "locations")
    private List<Person> persons;

    @OneToMany(mappedBy = "locations")
    private List<Address> addresses;
}
