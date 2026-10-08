package com.example.ticket_manager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ConcertRequestDTO {
    @NotBlank
    @Size(max = 120)
    private String name;
    @NotBlank
    @Size(max = 120)
    private String artist;
    @NotNull
    @Future
    private Date date;
    @NotBlank
    @Size(max = 120)
    private String venue;
    @NotEmpty
    @Size(max = 50)
    List<@Valid RowDetailRequestDTO> rows;
}
