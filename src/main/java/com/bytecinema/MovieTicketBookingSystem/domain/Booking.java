package com.bytecinema.MovieTicketBookingSystem.domain;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Instant dayBooking;
    private BigDecimal ticketPrice;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="screening_id")
    private Screening screening;

    @OneToMany(mappedBy = "booking")
    private List<ReservedBooking> reservedBookings;
    


}
