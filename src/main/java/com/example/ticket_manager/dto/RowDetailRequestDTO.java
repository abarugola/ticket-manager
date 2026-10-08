package com.example.ticket_manager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RowDetailRequestDTO {
    @NotBlank
    @Pattern(regexp = "[A-Z]{1,2}", message = "debe ser 1-2 letras mayúsculas")
    private String row;
    @Min(1)
    @Max(100)
    private int seatCount;
    @Min(1)
    @Max(100_000_000L)
    private long priceCent;
}
