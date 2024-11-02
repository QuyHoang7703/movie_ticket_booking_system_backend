package com.bytecinema.MovieTicketBookingSystem.controller;

import com.bytecinema.MovieTicketBookingSystem.domain.Booking;
import com.bytecinema.MovieTicketBookingSystem.dto.request.booking.ReqBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.booking.ResBooking;
import com.bytecinema.MovieTicketBookingSystem.service.BookingService;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/bookings")
    public ResponseEntity<ResBooking> createBooking(@RequestBody ReqBooking reqBooking) throws IdInValidException {
        Booking booking = bookingService.createBooking(reqBooking);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.bookingService.convertToResBooking(booking));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ResBooking> fetchBookingById(@RequestParam("bookingId") long bookingId) throws IdInValidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.bookingService.getBookingById(bookingId));
    }
}
