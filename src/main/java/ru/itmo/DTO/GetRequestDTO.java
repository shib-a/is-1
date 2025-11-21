package ru.itmo.DTO;

import lombok.Data;

@Data
public class GetRequestDTO {
    int pageNumber;
    int pageSize;
}
