package com.example.ticket_manager.dto;

import com.example.ticket_manager.domain.ConcertStatus;
import lombok.Builder;

import java.util.Date;

@Builder
public record ConcertSummary(String id, String name, String artist, Date date, String venue,
                             int totalSeats, int availableSeats, ConcertStatus availabilityStatus) {}
