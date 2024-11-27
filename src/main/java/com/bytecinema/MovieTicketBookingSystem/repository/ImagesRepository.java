package com.bytecinema.MovieTicketBookingSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bytecinema.MovieTicketBookingSystem.domain.Images;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImagesRepository extends JpaRepository<Images, Long> {
    void deleteByMovieId(Long movieId);
}
