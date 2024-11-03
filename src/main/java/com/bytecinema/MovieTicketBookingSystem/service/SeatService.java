package com.bytecinema.MovieTicketBookingSystem.service;

import com.bytecinema.MovieTicketBookingSystem.domain.Seat;
import com.bytecinema.MovieTicketBookingSystem.repository.SeatsRepository;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatsRepository seatsRepository;
    public void updateStatusSeat(long seatId, boolean status) throws IdInValidException {
        Seat seat = seatsRepository.findById(seatId)
                .orElseThrow(()-> new IdInValidException("Seat not found"));
        seat.setAvailable(status);
        this.seatsRepository.save(seat);
    }
}
