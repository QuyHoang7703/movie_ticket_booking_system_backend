package com.bytecinema.MovieTicketBookingSystem.domain;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    @ManyToOne
    @JoinColumn(name="auditorium_id")
    private Auditorium auditorium;

    @OneToMany(mappedBy = "seat")
    private List<ReservedBooking> reservedBookings;
}
