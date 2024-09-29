package com.bytecinema.MovieTicketBookingSystem.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="images")
@Getter
@Setter
public class Images {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
    private String imagePath;
    @ManyToOne
    @JoinColumn(name="movie_id")
    private Movie movie;


}
