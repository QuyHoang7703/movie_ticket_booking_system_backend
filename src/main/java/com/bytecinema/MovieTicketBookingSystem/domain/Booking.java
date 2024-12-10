package com.bytecinema.MovieTicketBookingSystem.domain;
import com.bytecinema.MovieTicketBookingSystem.util.constant.StatusPayment;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

import java.text.NumberFormat;
import java.time.Instant;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name="bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm dd-MM-yyy")
    private Instant timeBooking;
    private BigDecimal ticketPrice;
    private String transactionCode;

    private Instant paymentExpiryTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm dd-MM-yyy")
    private Instant paymentTime;

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
        this.timeBooking = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        this.paymentExpiryTime = Instant.now().plus(4, ChronoUnit.MINUTES);

    }

    @PreUpdate
    public void preUpdate(){
        this.paymentTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
    }

//    public String getFormattedTicketPrice() {
//        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//        return format.format(ticketPrice);
//    }

}
