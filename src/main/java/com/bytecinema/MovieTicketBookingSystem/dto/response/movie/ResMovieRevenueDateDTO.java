package com.bytecinema.MovieTicketBookingSystem.dto.response.movie;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMovieRevenueDateDTO {
    private long totalTicketsSold;
    private BigDecimal totalRevenue;
    private long movieId;
    private String movieName;
    private long totalTicket;
    private Instant time;
}
