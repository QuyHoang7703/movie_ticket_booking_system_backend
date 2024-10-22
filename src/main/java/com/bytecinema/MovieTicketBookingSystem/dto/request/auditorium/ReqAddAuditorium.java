package com.bytecinema.MovieTicketBookingSystem.dto.request.auditorium;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAddAuditorium {
    private String name;
    private int capacity;
    private int seatsPerRow;
}
