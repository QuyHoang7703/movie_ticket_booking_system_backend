package com.bytecinema.MovieTicketBookingSystem.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytecinema.MovieTicketBookingSystem.dto.request.screening.ReqAddScreeningDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.screening.ResScreeningDTO;
import com.bytecinema.MovieTicketBookingSystem.service.ScreeningService;

import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import java.util.List;
@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Validated
public class ScreeningController {
    private final ScreeningService screeningService;


    @PostMapping("/screening")
    public ResponseEntity<ResScreeningDTO> createScreening(@Valid @RequestBody ReqAddScreeningDTO request)
    {
        ResScreeningDTO res = screeningService.addScreening(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/screening")
    public ResponseEntity<List<ResScreeningDTO>> getAllScreenings()
    {
        List<ResScreeningDTO> res = screeningService.getAllScreen();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/screening/{id}")
    public ResponseEntity<ResScreeningDTO> getScreeningById(@PathVariable Long id)
    {
        ResScreeningDTO res = screeningService.getScreeningDTOById(id);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/screening/by-day")
    public ResponseEntity<List<ResScreeningDTO>> getScreeningByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        List<ResScreeningDTO> res = screeningService.getScreeningDTOByDay(date);
        return ResponseEntity.ok(res);
    }
}
