package com.bytecinema.MovieTicketBookingSystem.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="seats")
@Getter
@Setter
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int seatNumber;
    private String seatRow;
    private boolean available;

    @ManyToOne
    @JoinColumn(name="auditorium_id")
    private Auditorium auditorium;

    @ManyToMany(mappedBy = "seats", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Booking> bookings;

    @PrePersist
    public void prePersist(){
        this.available = true;
    }

}
