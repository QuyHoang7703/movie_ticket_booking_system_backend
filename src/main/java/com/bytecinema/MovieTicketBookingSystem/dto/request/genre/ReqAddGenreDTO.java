package com.bytecinema.MovieTicketBookingSystem.dto.request.genre;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAddGenreDTO {
    @NotBlank(message = "Name cannot be left blank")
    private String name;
    @NotBlank(message = "Description cannot be left blank")
    private String description;
}
