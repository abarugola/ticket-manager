package com.example.ticket_manager.dto;

import com.example.ticket_manager.domain.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDetailDTO {
    private String id;
    private String row;
    private int number;
    private long price;
    private SeatStatus status;
}