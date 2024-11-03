package com.bytecinema.MovieTicketBookingSystem.repository;

import com.bytecinema.MovieTicketBookingSystem.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    Optional<Booking> findByTransactionCode(String transactionCode);
}
