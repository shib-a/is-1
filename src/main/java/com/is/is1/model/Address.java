package com.is.is1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class Address {
    @NotBlank
    @Column(nullable = false)
    private String street;

    @NotNull
    @Valid
    private Location town;
}
