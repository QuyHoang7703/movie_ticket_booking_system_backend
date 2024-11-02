//package com.bytecinema.MovieTicketBookingSystem.domain;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.Table;
//import lombok.Getter;
//import lombok.Setter;
//
//@Entity
//@Table(name="reserved_booking")
//@Setter
//@Getter
//public class ReservedBooking {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private long id;
//
//    @ManyToOne
//    @JoinColumn(name="booking_id")
//    private Booking booking;
//
//    @ManyToOne
//    @JoinColumn(name="seat_id")
//    private Seat seat;
//
//}
