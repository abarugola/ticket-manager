package com.example.ticket_manager.mapper;

import com.example.ticket_manager.dto.SeatDetailDTO;
import com.example.ticket_manager.entity.Seat;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class SeatMapper {

    public SeatDetailDTO toSeatDTO(Seat seat) {
        if (seat == null) {
            return null;
        }

        return SeatDetailDTO.builder()
                .id(seat.getSeatId())
                .row(seat.getRow())
                .number(seat.getNumber())
                .price(seat.getPriceCents())
                .status(seat.getStatus())
                .build();
    }

    public List<SeatDetailDTO> toSeatDTOList(List<Seat> seats) {
        if (seats == null) {
            return List.of();
        }

        return seats.stream()
                .sorted(Comparator
                        .comparing(Seat::getRow)
                        .thenComparingInt(Seat::getNumber))
                .map(this::toSeatDTO)
                .toList();
    }
}


