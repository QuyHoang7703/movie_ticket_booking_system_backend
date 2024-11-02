package com.bytecinema.MovieTicketBookingSystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bytecinema.MovieTicketBookingSystem.domain.Auditorium;
import com.bytecinema.MovieTicketBookingSystem.domain.Seat;

@Repository
public interface SeatsRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByAuditorium(Auditorium auditorium);
    List<Seat> findByIdIn(List<Long> ids);
}
