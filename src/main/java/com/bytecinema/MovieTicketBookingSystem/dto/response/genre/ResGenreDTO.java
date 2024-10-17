package com.bytecinema.MovieTicketBookingSystem.dto.response.genre;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieDTO;

@Getter
@Setter
public class ResGenreDTO {
    private long id;
    private String name;
    private String description;
    List<ResMovieDTO> films;
}
