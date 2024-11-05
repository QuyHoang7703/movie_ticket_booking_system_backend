package com.bytecinema.MovieTicketBookingSystem.service;

import com.bytecinema.MovieTicketBookingSystem.domain.Booking;
import com.bytecinema.MovieTicketBookingSystem.domain.Screening;
import com.bytecinema.MovieTicketBookingSystem.domain.Seat;
import com.bytecinema.MovieTicketBookingSystem.repository.ScreeningsRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.SeatsRepository;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final ScreeningsRepository screeningsRepository;
    public List<Seat> getOrderedSeats(long screeningId) throws IdInValidException {
        List<Seat> orderedSeatList = new ArrayList<>();
        Screening screening = screeningsRepository.findById(screeningId)
                .orElse(null);
        List<Booking> bookingList = screening.getBookings();
        for (Booking booking : bookingList) {
            List<Seat> seatList = booking.getSeats();
            orderedSeatList.addAll(seatList);
        }
        return orderedSeatList;

    }
}
