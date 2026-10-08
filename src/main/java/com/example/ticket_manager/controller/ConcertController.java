package com.example.ticket_manager.controller;

import com.example.ticket_manager.dto.ConcertRequestDTO;
import com.example.ticket_manager.service.ConcertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @PostMapping
    public ResponseEntity<?> CreateConcert(@RequestBody @Valid ConcertRequestDTO concert){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(concertService.createConcert(concert));
    }
}
