package com.example.ticket_manager.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.example.ticket_manager.entity.Concert;
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
class ConcertRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @InjectMocks
    private ConcertRepository concertRepository;

    @Test
    void save_ShouldInvokeDynamoDBMapperSave() {
        // Arrange
        Concert concert = Concert.builder()
                .concertId("c101")
                .name("Rock Festival")
                .build();

        // Act
        Concert saved = concertRepository.save(concert);

        // Assert
        assertNotNull(saved);
        assertEquals("c101", saved.getConcertId());
        verify(dynamoDBMapper, times(1)).save(concert);
    }

    @Test
    void findById_ShouldInvokeDynamoDBMapperLoad() {
        // Arrange
        Concert concert = Concert.builder()
                .concertId("c101")
                .name("Rock Festival")
                .build();

        when(dynamoDBMapper.load(Concert.class, "c101")).thenReturn(concert);

        // Act
        Concert found = concertRepository.findById("c101");

        // Assert
        assertNotNull(found);
        assertEquals("c101", found.getConcertId());
        verify(dynamoDBMapper, times(1)).load(Concert.class, "c101");
    }

    @Test
    void findAll_ShouldInvokeDynamoDBMapperScan() {
        // Arrange
        @SuppressWarnings("unchecked")
        PaginatedScanList<Concert> mockScanList = mock(PaginatedScanList.class);
        when(dynamoDBMapper.scan(eq(Concert.class), any(DynamoDBScanExpression.class))).thenReturn(mockScanList);

        // Act
        List<Concert> result = concertRepository.findAll();

        // Assert
        assertNotNull(result);
        verify(dynamoDBMapper, times(1)).scan(eq(Concert.class), any(DynamoDBScanExpression.class));
    }
}