package com.bytecinema.MovieTicketBookingSystem.dto.response.seat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResSeatDTO {
    private long id;
    private int seatNumber;
    private String seatRow;
}
