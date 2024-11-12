package com.bytecinema.MovieTicketBookingSystem.dto.request.auditorium;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateAuditorium {
    @NotBlank(message = "Name cannot be blank")
    private String name;
}
