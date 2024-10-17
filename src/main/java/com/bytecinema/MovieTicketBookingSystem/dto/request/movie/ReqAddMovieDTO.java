package com.bytecinema.MovieTicketBookingSystem.dto.request.movie;



import lombok.Getter;
import lombok.Setter;
import java.util.List;

import java.time.Instant;
@Getter
@Setter
public class ReqAddMovieDTO {
    private String actors;
    private String nation;
    private String director;
    private String description;
    private String length;
    private String name;
    private Instant releaseDay;
    private List<Long> genreIds;
    private List<String> imagePaths;
}
