package com.bytecinema.MovieTicketBookingSystem.dto.request.movie;



import lombok.Getter;
import lombok.Setter;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
@Getter
@Setter
public class ReqAddMovieDTO {
    @NotNull(message = "Actors cannot be null")
    private String actors;

    @NotNull(message = "Nation cannot be null")
    private String nation;

    @NotNull(message = "Director cannot be null")
    private String director;

    @Size(max = 500, message = "Description should not exceed 500 characters")
    private String description;

    @NotNull(message = "Duration cannot be null")
    @Pattern(regexp = "^\\d+$", message = "Duration must contain only numbers")
    private String duration;

    @NotNull(message = "Name cannot be null")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Release day cannot be null")
    private Instant releaseDay;

    @NotNull(message = "PathTrailer cannot be null")
    private String pathTrailer;

    private List<Long> genreIds;

    private List<String> imagePaths;
}
