package com.example.ticket_manager.controller;

import com.example.ticket_manager.domain.ConcertStatus;
import com.example.ticket_manager.dto.ConcertRequestDTO;
import com.example.ticket_manager.dto.ConcertSummary;
import com.example.ticket_manager.dto.RowDetailRequestDTO;
import com.example.ticket_manager.service.ConcertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConcertControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConcertService concertService;

    @InjectMocks
    private ConcertController concertController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(concertController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createConcert_WhenValidRequest_ShouldReturn201Created() throws Exception {
        // Arrange: Fecha futura requerida por la anotación @Future
        Date futureDate = new Date(System.currentTimeMillis() + 86400000L);

        RowDetailRequestDTO rowDTO = new RowDetailRequestDTO("A", 10, 15000L);
        ConcertRequestDTO requestDTO = new ConcertRequestDTO("Rock Fest 2026", "The Rockers", futureDate, "Estadio Nacional", List.of(rowDTO));

        ConcertSummary summaryResponse = ConcertSummary.builder()
                .id("c101")
                .name("Rock Fest 2026")
                .artist("The Rockers")
                .date(futureDate)
                .venue("Estadio Nacional")
                .totalSeats(10)
                .availableSeats(10)
                .availabilityStatus(ConcertStatus.AVAILABLE)
                .build();

        when(concertService.createConcert(any(ConcertRequestDTO.class))).thenReturn(summaryResponse);

        // Act & Assert
        mockMvc.perform(post("/v1/concerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("c101"))
                .andExpect(jsonPath("$.name").value("Rock Fest 2026"))
                .andExpect(jsonPath("$.artist").value("The Rockers"))
                .andExpect(jsonPath("$.totalSeats").value(10));

        verify(concertService).createConcert(any(ConcertRequestDTO.class));
    }
}