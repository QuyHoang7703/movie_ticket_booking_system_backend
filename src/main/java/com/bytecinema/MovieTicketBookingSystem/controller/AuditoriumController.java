package com.bytecinema.MovieTicketBookingSystem.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytecinema.MovieTicketBookingSystem.dto.request.auditorium.ReqAddAuditorium;
import com.bytecinema.MovieTicketBookingSystem.dto.response.auditorium.ResAuditoriumDTO;
import com.bytecinema.MovieTicketBookingSystem.service.AuditoriumService;

import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class AuditoriumController {
    private final AuditoriumService auditoriumService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auditorium")
    public ResponseEntity<ResAuditoriumDTO> createAuditorium(@RequestBody ReqAddAuditorium req)
    {
        ResAuditoriumDTO res = auditoriumService.addAuditorium(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/auditorium/{id}")
    public ResponseEntity<ResAuditoriumDTO> getAuditoriumById(@PathVariable Long id)
    {
        ResAuditoriumDTO res = auditoriumService.getAuditoriumById(id);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/auditorium")
    public ResponseEntity<List<ResAuditoriumDTO>> getAllAuditoriums()
    {
        List<ResAuditoriumDTO> auditoriumDTOs = auditoriumService.getAuditoriums();
        return ResponseEntity.ok(auditoriumDTOs);
    }
}
