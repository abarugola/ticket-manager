package com.example.ticket_manager.controller;

import com.example.ticket_manager.domain.ConcertStatus;
import com.example.ticket_manager.dto.ConcertDetailDTO;
import com.example.ticket_manager.dto.ConcertSummary;
import com.example.ticket_manager.dto.ReservationRequestDTO;
import com.example.ticket_manager.dto.ReservationResponseDTO;
import com.example.ticket_manager.service.ConcertService;
import com.example.ticket_manager.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphQLControllerTest {

    @Mock
    private ConcertService concertService;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private GraphQLController graphQLController;

    private ConcertSummary concertSummary;

    @BeforeEach
    void setUp() {
        concertSummary = ConcertSummary.builder()
                .id("c101")
                .name("Rock Festival")
                .artist("The Band")
                .date(new Date())
                .venue("Estadio Central")
                .totalSeats(100)
                .availableSeats(50)
                .availabilityStatus(ConcertStatus.AVAILABLE)
                .build();
    }

    @Test
    void concerts_ShouldReturnAllConcerts() {
        when(concertService.getConcerts()).thenReturn(List.of(concertSummary));

        Iterable<ConcertSummary> result = graphQLController.concerts();

        assertNotNull(result);
        assertEquals(1, ((List<ConcertSummary>) result).size());
        verify(concertService, times(1)).getConcerts();
    }

    @Test
    void concertsByStatus_ShouldReturnConcertsFilteredByStatus() {
        when(concertService.getConcertsByStatus(ConcertStatus.AVAILABLE)).thenReturn(List.of(concertSummary));

        Iterable<ConcertSummary> result = graphQLController.concertsByStatus(ConcertStatus.AVAILABLE);

        assertNotNull(result);
        assertEquals(1, ((List<ConcertSummary>) result).size());
        verify(concertService, times(1)).getConcertsByStatus(ConcertStatus.AVAILABLE);
    }

    @Test
    void concertById_ShouldReturnConcert() {
        when(concertService.getConcertById("c101")).thenReturn(concertSummary);

        ConcertSummary result = graphQLController.concertById("c101");

        assertNotNull(result);
        assertEquals("c101", result.id());
        verify(concertService, times(1)).getConcertById("c101");
    }

    @Test
    void concertDetailById_ShouldReturnConcertDetail() {
        ConcertDetailDTO detailDTO = ConcertDetailDTO.builder()
                .concert(concertSummary)
                .availableSeats(List.of())
                .build();

        when(concertService.getConcertWithAvailableSeats("c101")).thenReturn(detailDTO);

        ConcertDetailDTO result = graphQLController.concertDetailById("c101");

        assertNotNull(result);
        assertEquals("c101", result.getConcert().id());
        verify(concertService, times(1)).getConcertWithAvailableSeats("c101");
    }

    @Test
    void confirmReservation_ShouldInvokeSeatService() {
        ReservationRequestDTO input = new ReservationRequestDTO("c101", List.of("A-1", "A-2"));
        ReservationResponseDTO expectedResponse = ReservationResponseDTO.builder()
                .success(true)
                .message("Reserva confirmada exitosamente.")
                .unavailableSeats(List.of())
                .build();

        when(seatService.confirmReservation(eq("Aldo"), eq("a.barugola.m@gmail.com"), any(ReservationRequestDTO.class)))
                .thenReturn(expectedResponse);

        ReservationResponseDTO response = graphQLController.confirmReservation(input);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Reserva confirmada exitosamente.", response.getMessage());
        verify(seatService, times(1)).confirmReservation(eq("Aldo"), eq("a.barugola.m@gmail.com"), eq(input));
    }
}