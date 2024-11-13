package com.bytecinema.MovieTicketBookingSystem.dto.response.auditorium;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.bytecinema.MovieTicketBookingSystem.dto.response.seat.ResSeatDTO;

@Getter
@Setter
public class ResAuditoriumDTO {
    private long id;
    private String name;
    private int capacity;
    private boolean status;
    List<ResSeatDTO> seats;
}
