package com.bytecinema.MovieTicketBookingSystem.dto.request.screening;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class ReqAddScreeningDTO {
    private Instant startTime;
    private BigDecimal ticketPrice;
    private Long movieId;
    private Long auditoriumId;
    private Integer adsDuration = 30;
}
