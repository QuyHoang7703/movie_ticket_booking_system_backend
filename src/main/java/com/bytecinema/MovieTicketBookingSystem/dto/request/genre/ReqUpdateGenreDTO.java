package com.bytecinema.MovieTicketBookingSystem.dto.request.genre;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateGenreDTO {
    private String name;
    private String description;
}
