package com.bytecinema.MovieTicketBookingSystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
// import org.hibernate.mapping.List;

@Entity
@Table(name="movies")
@Getter
@Setter
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    private Duration duration;
    private String actors;
    private String nation;
    private String director;
    private Instant releaseDay;
    private Instant createAt;
    private Instant updateAt;
    private String pathTrailer;
    private String language;
    
    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<MovieGenre> movieGenres;

  
    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<Screening> screenings;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<Images> images;

    @PrePersist
    public void handleBeforeCreated(){
      
        this.createAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updateAt = Instant.now();
    }

}
