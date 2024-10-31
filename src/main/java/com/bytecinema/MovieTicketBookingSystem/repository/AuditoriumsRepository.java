package com.bytecinema.MovieTicketBookingSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bytecinema.MovieTicketBookingSystem.domain.Auditorium;
import java.util.List;

@Repository
public interface AuditoriumsRepository extends JpaRepository<Auditorium, Long> {
    List<Auditorium> findByNameIgnoreCase(String name);
    List<Auditorium> findByNameStartingWithIgnoreCase(String name);
}
