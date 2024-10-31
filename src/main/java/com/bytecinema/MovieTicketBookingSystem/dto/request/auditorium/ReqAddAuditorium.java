package com.bytecinema.MovieTicketBookingSystem.dto.request.auditorium;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAddAuditorium {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Capacity cannot be null")
    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;

    @NotNull(message = "Seats per row cannot be null")
    @Min(value = 1, message = "Seats per row must be at least 1")
    private int seatsPerRow;
}
