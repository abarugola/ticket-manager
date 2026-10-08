package com.example.ticket_manager.mapper;

import com.example.ticket_manager.domain.ConcertStatus;
import com.example.ticket_manager.dto.ConcertRequestDTO;
import com.example.ticket_manager.dto.ConcertSummary;
import com.example.ticket_manager.dto.RowDetailRequestDTO;
import com.example.ticket_manager.entity.Concert;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConcertMapper {

    public ConcertSummary toSummary(Concert concert) {
        if (concert == null) {
            return null;
        }

        return ConcertSummary.builder()
                .id(concert.getConcertId())
                .name(concert.getName())
                .artist(concert.getArtist())
                .date(concert.getDate())
                .venue(concert.getVenue())
                .totalSeats(concert.getTotalSeats())
                .availableSeats(concert.availableSeats())
                .availabilityStatus(concert.getAvailabilityStatus())
                .build();
    }

    public List<ConcertSummary> toSummaryList(List<Concert> concerts) {
        if (concerts == null) {
            return List.of();
        }

        return concerts.stream()
                .map(this::toSummary)
                .toList();
    }

    public Concert toEntity(ConcertRequestDTO request) {
        if (request == null) {
            return null;
        }

        int totalSeats = 0;
        if (request.getRows() != null) {
            for (RowDetailRequestDTO rowDetail : request.getRows()) {
                totalSeats += rowDetail.getSeatCount();
            }
        }

        return Concert.builder()
                .name(request.getName())
                .artist(request.getArtist())
                .venue(request.getVenue())
                .date(request.getDate())
                .availabilityStatus(ConcertStatus.AVAILABLE)
                .totalSeats(totalSeats)
                .soldSeats(0)
                .build();
    }
}