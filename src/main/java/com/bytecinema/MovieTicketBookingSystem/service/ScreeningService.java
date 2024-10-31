package com.bytecinema.MovieTicketBookingSystem.service;

import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.dto.request.screening.ReqAddScreeningDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.screening.ResScreeningDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.AuditoriumsRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.MovieRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.ScreeningsRepository;
import com.bytecinema.MovieTicketBookingSystem.domain.Movie;
import lombok.RequiredArgsConstructor;
import com.bytecinema.MovieTicketBookingSystem.domain.Auditorium;
import com.bytecinema.MovieTicketBookingSystem.domain.Screening;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ScreeningService {
    private final ScreeningsRepository screeningsRepository;
    private final MovieRepository movieRepository;
    private final AuditoriumsRepository auditoriumsRepository;

    public ResScreeningDTO addScreening(ReqAddScreeningDTO request)
    {
        if (request.getStartTime().isBefore(Instant.now()))
        {
            throw new RuntimeException("Start time must be in the future");
        }
        Movie movie = movieRepository.findById(request.getMovieId())
            .orElseThrow(() -> new RuntimeException("Movie not found"));

        Auditorium auditorium = auditoriumsRepository.findById(request.getAuditoriumId())
            .orElseThrow(() -> new RuntimeException("Auditorium not found"));

        Instant endTime = request.getStartTime().plus(Duration.ofMinutes(request.getAdsDuration())).plus(movie.getDuration());

        boolean isOverLapping = screeningsRepository.existsByAuditoriumAndEndTimeGreaterThanAndStartTimeLessThan(auditorium, request.getStartTime(), endTime);

        if (isOverLapping)
        {
            throw new RuntimeException("There is already screening is this auditorium during the specified time.");
        }

        Screening screening = new Screening();
        screening.setStartTime(request.getStartTime());
        screening.setTicketPrice(request.getTicketPrice());
        screening.setMovie(movie);
        screening.setAuditorium(auditorium);
        screening.setEndTime(endTime);

        screeningsRepository.save(screening);


        ResScreeningDTO result = new ResScreeningDTO();
        result.setId(screening.getId());
        result.setStartTime(screening.getStartTime());
        result.setEndTime(screening.getEndTime());
        result.setMovieName(movie.getName());
        result.setAuditoriumName(auditorium.getName());
        result.setMovieId(movie.getId());
        result.setAuditoriumId(auditorium.getId());
        result.setTicketPrice(request.getTicketPrice());

        return result;
    }


    public List<ResScreeningDTO> getAllScreen()
    {
        List<Screening> screenings = screeningsRepository.findAll();

        return screenings.stream().map(screening -> {
            ResScreeningDTO dto = new ResScreeningDTO();
            dto.setId(screening.getId());
            dto.setStartTime(screening.getStartTime());
            dto.setEndTime(screening.getEndTime());
            dto.setTicketPrice(screening.getTicketPrice());
            dto.setMovieId(screening.getMovie().getId());
            dto.setMovieName(screening.getMovie().getName());
            dto.setAuditoriumId(screening.getAuditorium().getId());
            dto.setAuditoriumName(screening.getAuditorium().getName());
            return dto;
        }).collect(Collectors.toList());

    }
    public ResScreeningDTO getScreeningDTOById(Long id)
    {
        Screening screening = screeningsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Screening not found with id" + id));

        ResScreeningDTO dto = new ResScreeningDTO();
        dto.setId(screening.getId());
            dto.setStartTime(screening.getStartTime());
            dto.setEndTime(screening.getEndTime());
            dto.setTicketPrice(screening.getTicketPrice());
            dto.setMovieId(screening.getMovie().getId());
            dto.setMovieName(screening.getMovie().getName());
            dto.setAuditoriumId(screening.getAuditorium().getId());
            dto.setAuditoriumName(screening.getAuditorium().getName());

        return dto;
    }

    public List<ResScreeningDTO> getScreeningDTOByDay(LocalDate date)
    {
        Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusSeconds(1);

        List<Screening> screenings = screeningsRepository.findByStartTimeBetween(startOfDay, endOfDay);

        return screenings.stream().map(screening -> {
            ResScreeningDTO dto = new ResScreeningDTO();
            dto.setId(screening.getId());
            dto.setStartTime(screening.getStartTime());
            dto.setEndTime(screening.getEndTime());
            dto.setTicketPrice(screening.getTicketPrice());
            dto.setMovieId(screening.getMovie().getId());
            dto.setMovieName(screening.getMovie().getName());
            dto.setAuditoriumId(screening.getAuditorium().getId());
            dto.setAuditoriumName(screening.getAuditorium().getName());
            return dto;
        }).collect(Collectors.toList());
    }
}
