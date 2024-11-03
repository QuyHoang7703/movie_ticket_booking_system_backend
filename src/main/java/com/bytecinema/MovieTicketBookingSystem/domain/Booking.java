package com.bytecinema.MovieTicketBookingSystem.domain;
import com.bytecinema.MovieTicketBookingSystem.util.constant.StatusPayment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Instant timeBooking;
    private BigDecimal ticketPrice;
    private String transactionCode;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="screening_id")
    private Screening screening;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="booking_seats", joinColumns = @JoinColumn(name="booking_id"), inverseJoinColumns = @JoinColumn(name="seat_id"))
    private List<Seat> seats;

    @Enumerated(EnumType.STRING)
    private StatusPayment statusPayment;

    @PrePersist
    public void prePersist(){
        this.timeBooking = Instant.now();
    }

}
