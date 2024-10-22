package com.bytecinema.MovieTicketBookingSystem.dto.response.screening;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResScreeningDTO {
    private long id;              
    private Instant startTime;   
    private Instant endTime;      
    private BigDecimal ticketPrice; 
    private Long movieId;   
    private String movieName;    
    private Long auditoriumId;      
    private String auditoriumName;
}
