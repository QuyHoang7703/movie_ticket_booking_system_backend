package com.bytecinema.MovieTicketBookingSystem.dto.response.movie;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMovieDTO {
    private long id;
    private String name;
    private String length;
    private Instant releaseDay;
    private List<ResMovieGenreDTO> movieGenres;
    private List<String> imagePaths;
    private String actors;
    private String director;
    private String nation;
    private String description;

    // Constructors
    public ResMovieDTO() {}

    public ResMovieDTO(long id, String name, String length, Instant releaseDay, List<ResMovieGenreDTO> movieGenres) {
        this.id = id;
        this.name = name;
        this.length = length;
        this.releaseDay = releaseDay;
        this.movieGenres = movieGenres;
    }
}
