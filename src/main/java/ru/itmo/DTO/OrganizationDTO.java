package ru.itmo.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class OrganizationDTO {
    private Long id;
    private AddressDTO officialAddress;
    private Double annualTurnover;
    private Long employeesCount;
    private String fullName;
    private Float rating;
}
