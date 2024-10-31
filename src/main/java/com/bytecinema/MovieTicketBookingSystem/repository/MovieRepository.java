package com.bytecinema.MovieTicketBookingSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bytecinema.MovieTicketBookingSystem.domain.Movie;
import java.util.List;
import java.time.Instant;


@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>{
    List<Movie> findByReleaseDayAfter(Instant releaseDay);
    List<Movie> findByNameStartingWithIgnoreCase(String name);
    List<Movie> findByNameIgnoreCase(String name);
}
