package com.example.ticket_manager.service;

import com.example.ticket_manager.domain.ConcertStatus;
import com.example.ticket_manager.dto.ConcertDetailDTO;
import com.example.ticket_manager.dto.ConcertRequestDTO;
import com.example.ticket_manager.dto.ConcertSummary;
import com.example.ticket_manager.dto.RowDetailRequestDTO;
import com.example.ticket_manager.entity.Concert;
import com.example.ticket_manager.entity.Seat;
import com.example.ticket_manager.mapper.ConcertMapper;
import com.example.ticket_manager.mapper.SeatMapper;
import com.example.ticket_manager.repository.ConcertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertMapper concertMapper;

    @Mock
    private SeatMapper seatMapper;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private ConcertService concertService;

    private Concert concert;
    private ConcertSummary concertSummary;

    @BeforeEach
    void setUp() {
        Date now = new Date();
        concert = Concert.builder()
                .concertId("c101")
                .name("World Tour 2026")
                .artist("Coldplay")
                .venue("Estadio")
                .date(now)
                .totalSeats(100)
                .soldSeats(10)
                .availabilityStatus(ConcertStatus.AVAILABLE)
                .build();

        concertSummary = ConcertSummary.builder()
                .id("c101")
                .name("World Tour 2026")
                .artist("Coldplay")
                .venue("Estadio")
                .date(now)
                .totalSeats(100)
                .availableSeats(90)
                .availabilityStatus(ConcertStatus.AVAILABLE)
                .build();
    }

    @Test
    void createConcert_ShouldSaveAndGenerateSeats() {
        // Arrange
        RowDetailRequestDTO row = new RowDetailRequestDTO("A", 10, 5000L);
        ConcertRequestDTO request = new ConcertRequestDTO("World Tour 2026", "Coldplay", new Date(), "Estadio", List.of(row));

        when(concertMapper.toEntity(request)).thenReturn(concert);
        when(concertRepository.save(concert)).thenReturn(concert);
        when(concertMapper.toSummary(concert)).thenReturn(concertSummary);

        // Act
        ConcertSummary result = concertService.createConcert(request);

        // Assert
        assertNotNull(result);
        assertEquals("c101", result.id());
        verify(concertRepository, times(1)).save(concert);
        verify(seatService, times(1)).createSeatsForConcert("c101", request.getRows());
    }

    @Test
    void getConcertWithAvailableSeats_WhenConcertExists_ShouldReturnDetail() {
        // Arrange
        List<Seat> availableSeats = List.of();
        when(concertRepository.findById("c101")).thenReturn(concert);
        when(concertMapper.toSummary(concert)).thenReturn(concertSummary);
        when(seatService.getAvailableSeatsByConcertId("c101")).thenReturn(availableSeats);
        when(seatMapper.toSeatDTOList(availableSeats)).thenReturn(List.of());

        // Act
        ConcertDetailDTO detail = concertService.getConcertWithAvailableSeats("c101");

        // Assert
        assertNotNull(detail);
        assertEquals("c101", detail.getConcert().id());
        verify(concertRepository).findById("c101");
        verify(seatService).getAvailableSeatsByConcertId("c101");
    }

    @Test
    void getConcertWithAvailableSeats_WhenNotFound_ShouldReturnNull() {
        // Arrange
        when(concertRepository.findById("invalid-id")).thenReturn(null);

        // Act
        ConcertDetailDTO detail = concertService.getConcertWithAvailableSeats("invalid-id");

        // Assert
        assertNull(detail);
        verify(concertRepository).findById("invalid-id");
        verifyNoInteractions(seatService);
    }
}