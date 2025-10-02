package com.is.is1;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class Organization {
    @NotNull
    private Address officialAddress;
    @Min(0)
    @NotNull
    private Float annualTurnover;
    @Min(0)
    private long employeesCount;
    private String fullName;
    @Min(0)
    private int rating;
}
