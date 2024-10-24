package com.bytecinema.MovieTicketBookingSystem.dto.response.movie;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResMovieGenreDTO {
    private String name;
    private String description;
    private Long id;

    // Constructors
    public ResMovieGenreDTO() {}

    public ResMovieGenreDTO(String name, String description, Long id) {
        this.name = name;
        this.description = description;
        this.id = id;
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
