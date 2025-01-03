package com.bytecinema.MovieTicketBookingSystem.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.bytecinema.MovieTicketBookingSystem.domain.Genre;
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long>{
    Optional<Genre> findByName(String name);
    List<Genre> findByNameIgnoreCase(String name);
}
