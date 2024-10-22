package com.bytecinema.MovieTicketBookingSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bytecinema.MovieTicketBookingSystem.domain.Auditorium;


@Repository
public interface AuditoriumsRepository extends JpaRepository<Auditorium, Long> {
    
}
