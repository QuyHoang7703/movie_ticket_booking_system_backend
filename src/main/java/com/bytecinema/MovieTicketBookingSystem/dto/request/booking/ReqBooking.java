package com.bytecinema.MovieTicketBookingSystem.dto.request.booking;

import com.bytecinema.MovieTicketBookingSystem.domain.Seat;
import lombok.Data;

import java.util.List;
@Data
public class ReqBooking {
    private long screeningId;
    private List<Seat> seats;
}
