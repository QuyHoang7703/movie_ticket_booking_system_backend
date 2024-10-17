package com.bytecinema.MovieTicketBookingSystem.dto.response.movie;

public class ResMovieGenreDTO {
    private String name;
    private String description;

    // Constructors
    public ResMovieGenreDTO() {}

    public ResMovieGenreDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
