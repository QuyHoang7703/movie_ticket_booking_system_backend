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
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="genres")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    private Instant createAt;
    private Instant updateAt;



    @PrePersist
    public void handleBeforeCreated(){
      
        this.createAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updateAt = Instant.now();
    }
    
    @OneToMany(mappedBy = "genre")
    private List<MovieGenre> movieGenres;

    public void setDescription(String description) {
        this.description = description;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
}
