package com.example.ticket_manager.controller;

import com.example.ticket_manager.domain.ConcertStatus;
import com.example.ticket_manager.dto.ConcertDetailDTO;
import com.example.ticket_manager.dto.ConcertSummary;
import com.example.ticket_manager.dto.ReservationRequestDTO;
import com.example.ticket_manager.dto.ReservationResponseDTO;
import com.example.ticket_manager.service.ConcertService;
import com.example.ticket_manager.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GraphQLController {

    private final ConcertService concertService;
    private final SeatService seatService;

    @QueryMapping
    Iterable<ConcertSummary> concerts() {
        return concertService.getConcerts();
    }

    @QueryMapping
    Iterable<ConcertSummary> concertsByStatus(@Argument ConcertStatus status) {
        return concertService.getConcertsByStatus(status);
    }

    @QueryMapping
    ConcertSummary concertById(@Argument String id) {
        return concertService.getConcertById(id);
    }

    @QueryMapping
    ConcertDetailDTO concertDetailById(@Argument String id) {
        return concertService.getConcertWithAvailableSeats(id);
    }

    // Mutation con authorizacion
//    @MutationMapping
//    @PreAuthorize("isAuthenticated()")
//    public ReservationResponseDTO confirmReservation(
//            @Argument ReservationRequestDTO input,
//            @AuthenticationPrincipal Jwt jwt
//    ) {
//        if (jwt == null) {
//            throw new AuthenticationCredentialsNotFoundException("Token JWT requerido o inválido");
//        }
//        String userEmail = jwt.getSubject();
//        String userName = jwt.getClaimAsString("name");
//
//        return seatService.confirmReservation(userName, userEmail, input);
//    }

    @MutationMapping
    public ReservationResponseDTO confirmReservation(
            @Argument ReservationRequestDTO input
    ) {
        String userEmail = "a.barugola.m@gmail.com";
        String userName = "Aldo";

        return seatService.confirmReservation(userName, userEmail, input);
    }
}
