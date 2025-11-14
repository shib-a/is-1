package com.is.is1.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class Location {
    private int x;                                 // primitive → default 0

    @NotNull
    @Column(nullable = false)
    private Integer y;

    private int z;                                 // primitive → default 0

    private String name;
}
