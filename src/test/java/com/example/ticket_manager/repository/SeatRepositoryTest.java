package com.example.ticket_manager.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.example.ticket_manager.domain.SeatStatus;
import com.example.ticket_manager.entity.Seat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @InjectMocks
    private SeatRepository seatRepository;

    private Seat seat;

    @BeforeEach
    void setUp() {
        seat = Seat.builder()
                .concertId("concert-1")
                .seatId("A-1")
                .row("A")
                .number(1)
                .priceCents(5000L)
                .status(SeatStatus.AVAILABLE)
                .build();
    }

    @Test
    void save_ShouldInvokeSaveOnDynamoDBMapper() {
        Seat savedSeat = seatRepository.save(seat);

        assertNotNull(savedSeat);
        assertEquals("A-1", savedSeat.getSeatId());
        verify(dynamoDBMapper, times(1)).save(seat);
    }

    @Test
    void saveAll_ShouldInvokeBatchSaveOnDynamoDBMapper() {
        List<Seat> seats = List.of(seat);

        List<Seat> result = seatRepository.saveAll(seats);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(dynamoDBMapper, times(1)).batchSave(seats);
    }

    @Test
    void findByConcertIdAndSeatId_ShouldInvokeLoadOnDynamoDBMapper() {
        when(dynamoDBMapper.load(Seat.class, "concert-1", "A-1")).thenReturn(seat);

        Seat foundSeat = seatRepository.findByConcertIdAndSeatId("concert-1", "A-1");

        assertNotNull(foundSeat);
        assertEquals("concert-1", foundSeat.getConcertId());
        assertEquals("A-1", foundSeat.getSeatId());
        verify(dynamoDBMapper, times(1)).load(Seat.class, "concert-1", "A-1");
    }

    @Test
    void findAllByConcertId_ShouldQueryDynamoDBMapper() {
        @SuppressWarnings("unchecked")
        PaginatedQueryList<Seat> mockQueryList = mock(PaginatedQueryList.class);
        when(dynamoDBMapper.query(eq(Seat.class), any(DynamoDBQueryExpression.class))).thenReturn(mockQueryList);

        List<Seat> seats = seatRepository.findAllByConcertId("concert-1");

        assertNotNull(seats);
        verify(dynamoDBMapper, times(1)).query(eq(Seat.class), any(DynamoDBQueryExpression.class));
    }

    @Test
    void findByConcertIdAndStatus_ShouldQueryWithIndexName() {
        @SuppressWarnings("unchecked")
        PaginatedQueryList<Seat> mockQueryList = mock(PaginatedQueryList.class);
        when(dynamoDBMapper.query(eq(Seat.class), any(DynamoDBQueryExpression.class))).thenReturn(mockQueryList);

        List<Seat> seats = seatRepository.findByConcertIdAndStatus("concert-1", SeatStatus.AVAILABLE);

        assertNotNull(seats);
        verify(dynamoDBMapper, times(1)).query(eq(Seat.class), any(DynamoDBQueryExpression.class));
    }

    @Test
    void findAvailableSeats_ShouldCallFindByConcertIdAndStatus() {
        @SuppressWarnings("unchecked")
        PaginatedQueryList<Seat> mockQueryList = mock(PaginatedQueryList.class);
        when(dynamoDBMapper.query(eq(Seat.class), any(DynamoDBQueryExpression.class))).thenReturn(mockQueryList);

        List<Seat> seats = seatRepository.findAvailableSeats("concert-1");

        assertNotNull(seats);
        verify(dynamoDBMapper, times(1)).query(eq(Seat.class), any(DynamoDBQueryExpression.class));
    }

    @Test
    void saveSeatIfAvailable_ShouldInvokeSaveWithExpectedCondition() {
        seatRepository.saveSeatIfAvailable(seat);

        verify(dynamoDBMapper, times(1)).save(eq(seat), any(DynamoDBSaveExpression.class));
    }
}