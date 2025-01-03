package com.bytecinema.MovieTicketBookingSystem.dto.response.movie;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMovieRevenueDTO {
    private long totalTicketsSold;
    private BigDecimal totalRevenue;
    private long movieId;
    private String movieName;
    private long totalTicket;
}
