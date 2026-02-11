package ru.itmo.DTO;

import lombok.Data;
import ru.itmo.model.ImportStatus;

import java.time.LocalDateTime;

@Data
public class ImportHistoryDTO {
    private Long id;
    private LocalDateTime timestamp;
    private ImportStatus status;
    private Integer addedCount;
    private String errorMessage;
    private String fileName;
}
