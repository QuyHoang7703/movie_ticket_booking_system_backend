package com.bytecinema.MovieTicketBookingSystem.dto.response.movie;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMovieRevenueDTO {
    private long totalTicketsSold;
    private BigDecimal totalRevenue;

    public ResMovieRevenueDTO(long totalTicketsSold, BigDecimal totalRevenue) {
        this.totalTicketsSold = totalTicketsSold;
        this.totalRevenue = totalRevenue;
    }
}
