package com.example.ticket_manager.service;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.example.ticket_manager.domain.SeatStatus;
import com.example.ticket_manager.dto.ReservationRequestDTO;
import com.example.ticket_manager.dto.ReservationResponseDTO;
import com.example.ticket_manager.dto.RowDetailRequestDTO;
import com.example.ticket_manager.entity.Seat;
import com.example.ticket_manager.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatService seatService;

    private Seat availableSeat1;
    private Seat availableSeat2;

    @BeforeEach
    void setUp() {
        availableSeat1 = Seat.builder()
                .concertId("c101")
                .seatId("A-1")
                .row("A")
                .number(1)
                .priceCents(1000L)
                .status(SeatStatus.AVAILABLE)
                .build();

        availableSeat2 = Seat.builder()
                .concertId("c101")
                .seatId("A-2")
                .row("A")
                .number(2)
                .priceCents(1000L)
                .status(SeatStatus.AVAILABLE)
                .build();
    }

    @Test
    void createSeatsForConcert_WhenRowsProvided_ShouldBuildAndSaveSeats() {
        RowDetailRequestDTO rowA = new RowDetailRequestDTO("A", 2, 1500L);
        when(seatRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Seat> createdSeats = seatService.createSeatsForConcert("c101", List.of(rowA));

        assertNotNull(createdSeats);
        assertEquals(2, createdSeats.size());
        assertEquals("A-1", createdSeats.get(0).getSeatId());
        assertEquals("A-2", createdSeats.get(1).getSeatId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Seat>> captor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void createSeatsForConcert_WhenRowsEmpty_ShouldReturnEmptyList() {
        List<Seat> result = seatService.createSeatsForConcert("c101", List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(seatRepository);
    }

    @Test
    void getSeatsByConcertId_ShouldReturnSeatsFromRepository() {
        when(seatRepository.findAllByConcertId("c101")).thenReturn(List.of(availableSeat1, availableSeat2));

        List<Seat> seats = seatService.getSeatsByConcertId("c101");

        assertEquals(2, seats.size());
        verify(seatRepository).findAllByConcertId("c101");
    }

    @Test
    void getAvailableSeatsByConcertId_ShouldReturnAvailableSeats() {
        when(seatRepository.findAvailableSeats("c101")).thenReturn(List.of(availableSeat1));

        List<Seat> seats = seatService.getAvailableSeatsByConcertId("c101");

        assertEquals(1, seats.size());
        verify(seatRepository).findAvailableSeats("c101");
    }

    @Test
    void confirmReservation_WhenNoSeatsSelected_ShouldReturnFailure() {
        ReservationRequestDTO request = new ReservationRequestDTO("c101", List.of());

        ReservationResponseDTO response = seatService.confirmReservation("Aldo", "a.barugola.m@gmail.com", request);

        assertFalse(response.isSuccess());
        assertEquals("Debe seleccionar al menos un asiento.", response.getMessage());
        assertTrue(response.getUnavailableSeats().isEmpty());
    }

    @Test
    void confirmReservation_WhenAllSeatsAvailable_ShouldConfirmReservation() {
        ReservationRequestDTO request = new ReservationRequestDTO("c101", List.of("A-1", "A-2"));

        when(seatRepository.findByConcertIdAndSeatId("c101", "A-1")).thenReturn(availableSeat1);
        when(seatRepository.findByConcertIdAndSeatId("c101", "A-2")).thenReturn(availableSeat2);

        ReservationResponseDTO response = seatService.confirmReservation("Aldo", "a.barugola.m@gmail.com", request);

        assertTrue(response.isSuccess());
        assertEquals("Reserva confirmada exitosamente.", response.getMessage());
        assertTrue(response.getUnavailableSeats().isEmpty());
        verify(seatRepository, times(1)).saveSeatIfAvailable(availableSeat1);
        verify(seatRepository, times(1)).saveSeatIfAvailable(availableSeat2);
    }

    @Test
    void confirmReservation_WhenSeatAlreadySoldOrNotAvailable_ShouldRollbackAndReturnFailure() {
        Seat soldSeat = Seat.builder()
                .concertId("c101")
                .seatId("A-2")
                .row("A")
                .number(2)
                .status(SeatStatus.SOLD)
                .build();

        ReservationRequestDTO request = new ReservationRequestDTO("c101", List.of("A-1", "A-2"));

        when(seatRepository.findByConcertIdAndSeatId("c101", "A-1")).thenReturn(availableSeat1);
        when(seatRepository.findByConcertIdAndSeatId("c101", "A-2")).thenReturn(soldSeat);

        ReservationResponseDTO response = seatService.confirmReservation("Aldo", "a.barugola.m@gmail.com", request);

        assertFalse(response.isSuccess());
        assertEquals("No se pudo completar la reserva. Algunos asientos ya fueron reservados por otro usuario.", response.getMessage());
        assertTrue(response.getUnavailableSeats().contains("A-2"));

        // Rollback verification
        verify(seatRepository).save(availableSeat1);
        assertEquals(SeatStatus.AVAILABLE, availableSeat1.getStatus());
    }

    @Test
    void confirmReservation_WhenConditionalCheckFails_ShouldRollbackAndReturnFailure() {
        ReservationRequestDTO request = new ReservationRequestDTO("c101", List.of("A-1"));

        when(seatRepository.findByConcertIdAndSeatId("c101", "A-1")).thenReturn(availableSeat1);
        doThrow(new ConditionalCheckFailedException("Already reserved")).when(seatRepository).saveSeatIfAvailable(availableSeat1);

        ReservationResponseDTO response = seatService.confirmReservation("Aldo", "a.barugola.m@gmail.com", request);

        assertFalse(response.isSuccess());
        assertTrue(response.getUnavailableSeats().contains("A-1"));
    }
}