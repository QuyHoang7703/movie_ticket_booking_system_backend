package com.bytecinema.MovieTicketBookingSystem.repository;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bytecinema.MovieTicketBookingSystem.domain.Auditorium;
import com.bytecinema.MovieTicketBookingSystem.domain.Screening;

@Repository
public interface ScreeningsRepository extends JpaRepository<Screening, Long> {
    boolean existsByAuditoriumAndStartTimeBetween(Auditorium auditorium, Instant startTime, Instant endTime);
}
