package com.bytecinema.MovieTicketBookingSystem.repository;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bytecinema.MovieTicketBookingSystem.domain.Auditorium;
import com.bytecinema.MovieTicketBookingSystem.domain.Screening;
import java.util.List;

@Repository
public interface ScreeningsRepository extends JpaRepository<Screening, Long> {
    boolean existsByAuditoriumAndEndTimeGreaterThanAndStartTimeLessThan(Auditorium auditorium, Instant startTime, Instant endTime);
    boolean existsByIdNotAndAuditoriumAndEndTimeGreaterThanAndStartTimeLessThan(Long id, Auditorium auditorium, Instant endTime, Instant startTime);
    List<Screening> findByStartTimeBetween(Instant startOfDay, Instant endOfDay);
}
