package com.example.ticket_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertDetailDTO {
    private ConcertSummary concert;
    private List<SeatDetailDTO> availableSeats;
}