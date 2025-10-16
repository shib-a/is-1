package com.is.is1;

import jakarta.persistence.*;
import jakarta.validation.Constraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class Person {
    @NotNull
    @Enumerated(EnumType.STRING)
    private Color eyeColor;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Color hairColor;
    @NotNull
    @Embedded
    private Location location;
    @Min(0)
    private long height;
    @Column(name = "passport_id")
    private String passportID; // change
    @NotNull
    @Enumerated(EnumType.STRING)
    private Country nationality;
}
