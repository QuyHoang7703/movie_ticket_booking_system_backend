package com.bytecinema.MovieTicketBookingSystem.controller;

import com.bytecinema.MovieTicketBookingSystem.domain.Seat;
import com.bytecinema.MovieTicketBookingSystem.service.SeatService;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class SeatController {
    private final SeatService seatService;

    @GetMapping("/seats")
    public ResponseEntity<List<Seat>> fetchOrderedSeats(@RequestParam("screeningId") long screeningId) throws IdInValidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.seatService.getOrderedSeats(screeningId));
    }
}
