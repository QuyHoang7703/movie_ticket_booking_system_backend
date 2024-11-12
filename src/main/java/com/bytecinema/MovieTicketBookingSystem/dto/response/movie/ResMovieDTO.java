package com.bytecinema.MovieTicketBookingSystem.dto.response.movie;

import java.time.Instant;
import java.util.List;

import com.bytecinema.MovieTicketBookingSystem.dto.response.screening.ResScreeningDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMovieDTO {
    private long id;
    private String name;
    private String duration;
    private Instant releaseDay;
    private List<ResMovieGenreDTO> movieGenres;
    private List<String> imagePaths;
    private String actors;
    private String director;
    private String nation;
    private String description;
    private List<ResScreeningDTO> screenings;
    private String pathTrailer;
    private String language;

    // Constructors
    public ResMovieDTO() {}

    public ResMovieDTO(long id, String name, String duration, Instant releaseDay, List<ResMovieGenreDTO> movieGenres) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.releaseDay = releaseDay;
        this.movieGenres = movieGenres;
    }
}
