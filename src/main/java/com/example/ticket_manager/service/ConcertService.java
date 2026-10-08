package com.example.ticket_manager.service;

import com.example.ticket_manager.domain.ConcertStatus;
import com.example.ticket_manager.dto.ConcertDetailDTO;
import com.example.ticket_manager.dto.ConcertRequestDTO;
import com.example.ticket_manager.dto.ConcertSummary;
import com.example.ticket_manager.entity.Concert;
import com.example.ticket_manager.entity.Seat;
import com.example.ticket_manager.mapper.ConcertMapper;
import com.example.ticket_manager.mapper.SeatMapper;
import com.example.ticket_manager.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertMapper concertMapper;
    private final SeatMapper seatMapper;
    private final SeatService seatService;

    public ConcertSummary createConcert(ConcertRequestDTO request) {
        Concert concert = concertMapper.toEntity(request);
        concertRepository.save(concert);

        seatService.createSeatsForConcert(concert.getConcertId(), request.getRows());

        return concertMapper.toSummary(concert);
    }

    public List<ConcertSummary> getConcerts() {
        List<Concert> concerts = concertRepository.findAll();

        return concertMapper.toSummaryList(concerts);
    }

    public List<ConcertSummary> getConcertsByStatus(ConcertStatus status) {
        List<Concert> concerts = concertRepository.findByAvailabilityStatus(status);
        return concertMapper.toSummaryList(concerts);
    }

    public ConcertSummary getConcertById(String id) {
        Concert concert = concertRepository.findById(id);

        return concertMapper.toSummary(concert);
    }

    public ConcertDetailDTO getConcertWithAvailableSeats(String id) {
        Concert concert = concertRepository.findById(id);

        if (concert == null) {
            return null;
        }

        ConcertSummary concertSummary = concertMapper.toSummary(concert);
        List<Seat> availableSeats = seatService.getAvailableSeatsByConcertId(id);

        return ConcertDetailDTO.builder()
                .concert(concertSummary)
                .availableSeats(seatMapper.toSeatDTOList(availableSeats))
                .build();
    }
}
