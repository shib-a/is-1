package com.is.is1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class Coordinates {
    @NotNull
    @DecimalMin(value = "-573", inclusive = false)   // x > -573
    @Column(nullable = false)
    private Double x;

    @NotNull
    @DecimalMin(value = "-236", inclusive = false)   // y > -236
    @Column(nullable = false)
    private Double y;
}
