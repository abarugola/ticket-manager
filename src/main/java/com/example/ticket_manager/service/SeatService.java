package com.example.ticket_manager.service;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.example.ticket_manager.domain.SeatStatus;
import com.example.ticket_manager.dto.ReservationRequestDTO;
import com.example.ticket_manager.dto.ReservationResponseDTO;
import com.example.ticket_manager.dto.RowDetailRequestDTO;
import com.example.ticket_manager.entity.Seat;
import com.example.ticket_manager.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    public List<Seat> createSeatsForConcert(String concertId, List<RowDetailRequestDTO> rowDetails) {
        if (rowDetails == null || rowDetails.isEmpty()) {
            return List.of();
        }

        List<Seat> seatsToSave = new ArrayList<>();

        for (RowDetailRequestDTO rowDetail : rowDetails) {
            String rowName = rowDetail.getRow();
            int seatCount = rowDetail.getSeatCount();
            long priceCents = rowDetail.getPriceCent();

            for (int seatNum = 1; seatNum <= seatCount; seatNum++) {
                String seatId = String.format("%s-%d", rowName, seatNum);

                Seat seat = Seat.builder()
                        .concertId(concertId)
                        .seatId(seatId)
                        .row(rowName)
                        .number(seatNum)
                        .priceCents(priceCents)
                        .status(SeatStatus.AVAILABLE)
                        .build();

                seatsToSave.add(seat);
            }
        }

        return seatRepository.saveAll(seatsToSave);
    }

    public List<Seat> getSeatsByConcertId(String concertId) {
        return seatRepository.findAllByConcertId(concertId);
    }

    public List<Seat> getAvailableSeatsByConcertId(String concertId) {
        return seatRepository.findAvailableSeats(concertId);
    }

    public ReservationResponseDTO confirmReservation(String userName, String userEmail,ReservationRequestDTO request) {
        String concertId = request.getConcertId();
        List<String> requestedSeatIds = request.getSeatIds();

        if (requestedSeatIds == null || requestedSeatIds.isEmpty()) {
            return ReservationResponseDTO.builder()
                    .success(false)
                    .message("Debe seleccionar al menos un asiento.")
                    .unavailableSeats(List.of())
                    .build();
        }

        System.out.println("------> Confirmacion de pasarela de Pago");

        List<String> successfullyReserved = new ArrayList<>();
        List<String> unavailableSeats = new ArrayList<>();

        for (String seatId : requestedSeatIds) {
            Seat seat = seatRepository.findByConcertIdAndSeatId(concertId, seatId);

            if (seat == null || seat.getStatus() != SeatStatus.AVAILABLE) {
                unavailableSeats.add(seatId);
                continue;
            }

            seat.setStatus(SeatStatus.SOLD);

            try {
                seatRepository.saveSeatIfAvailable(seat);
                successfullyReserved.add(seatId);
            } catch (ConditionalCheckFailedException e) {
                //Asiento reservado
                unavailableSeats.add(seatId);
            }
        }

        //Rollback si por lo menos un asiento ya estaba reservado
        if (!unavailableSeats.isEmpty()) {
            for (String reservedSeatId : successfullyReserved) {
                Seat seatToRollback = seatRepository.findByConcertIdAndSeatId(concertId, reservedSeatId);
                if (seatToRollback != null) {
                    seatToRollback.setStatus(SeatStatus.AVAILABLE);
                    seatRepository.save(seatToRollback);
                }
            }

            System.out.println("------> Devolucion de Pago");

            return ReservationResponseDTO.builder()
                    .success(false)
                    .message("No se pudo completar la reserva. Algunos asientos ya fueron reservados por otro usuario.")
                    .unavailableSeats(unavailableSeats)
                    .build();
        }

        System.out.println("-----> Evento de confirmacion de reservacion");
        System.out.println("Consumers: \n" +
                "- Notificar al usuario\n" +
                "- Actualizar información del concierto\n");

        return ReservationResponseDTO.builder()
                .success(true)
                .message("Reserva confirmada exitosamente.")
                .unavailableSeats(List.of())
                .build();
    }
}