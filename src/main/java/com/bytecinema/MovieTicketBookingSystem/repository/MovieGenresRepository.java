package com.bytecinema.MovieTicketBookingSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.bytecinema.MovieTicketBookingSystem.domain.MovieGenre;

@Repository
public interface MovieGenresRepository extends JpaRepository<MovieGenre, Long> {
    List<MovieGenre> findByGenreId(Long genreId);
    void deleteByMovieId(Long movieId);
}
