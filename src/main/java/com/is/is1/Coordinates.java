package com.is.is1;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;

@Embeddable
public class Coordinates {
    @Min(-573)
    private Double x;
    @Min(-263)
    private Double y;
}
