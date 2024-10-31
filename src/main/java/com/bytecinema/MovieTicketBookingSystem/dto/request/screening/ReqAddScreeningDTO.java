package com.bytecinema.MovieTicketBookingSystem.dto.request.screening;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class ReqAddScreeningDTO {

    @NotNull(message = "Start time cannot be null")
    @Future(message = "Start time must be in the future")
    private Instant startTime;

    @NotNull(message = "Ticket price cannot be null")
    @Positive(message = "Ticket must be position")
    private BigDecimal ticketPrice;

    @NotNull(message = "Movie ID cannot be null")
    @Positive(message = "Movie ID must be a positive number")
    private Long movieId;

    @NotNull(message = "Auditorium ID cannot be null")
    @Positive(message = "Auditorium ID must be a positive number")
    private Long auditoriumId;

    @NotNull(message = "Ads duration cannot be null")
    @Min(value = 0, message = "Ads duration must be at least 0 minutes")
    private Integer adsDuration = 30;
}
