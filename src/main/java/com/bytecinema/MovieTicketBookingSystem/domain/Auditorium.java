package com.bytecinema.MovieTicketBookingSystem.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name="auditoriums")
@Getter
@Setter
public class Auditorium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private int capacity;
    
    @OneToMany(mappedBy = "auditorium", fetch = FetchType.LAZY)
    private List<Seat> seats;

    @OneToMany(mappedBy = "auditorium", fetch = FetchType.LAZY)
    private List<Screening> screenings;
    
}
