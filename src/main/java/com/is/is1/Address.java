package com.is.is1;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class Address {
    @NotNull
    private String street;
    @NotNull
    private Location town;
}
