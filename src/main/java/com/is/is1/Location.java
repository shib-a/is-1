package com.is.is1;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class Location {
    @NotNull
    private Long x;
    private double y;
    @NotNull
    private Long z;
    @NotNull
    private String name;
}
