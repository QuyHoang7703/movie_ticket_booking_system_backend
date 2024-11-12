package com.bytecinema.MovieTicketBookingSystem.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytecinema.MovieTicketBookingSystem.dto.request.auditorium.ReqAddAuditorium;
import com.bytecinema.MovieTicketBookingSystem.dto.response.auditorium.ResAuditoriumDTO;
import com.bytecinema.MovieTicketBookingSystem.service.AuditoriumService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Validated
public class AuditoriumController {
    private final AuditoriumService auditoriumService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auditorium")
    public ResponseEntity<ResAuditoriumDTO> createAuditorium(@Valid @RequestBody ReqAddAuditorium req)
    {
        ResAuditoriumDTO res = auditoriumService.addAuditorium(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/auditorium/{id}")
    public ResponseEntity<Void> deleteAuditorium(@PathVariable Long id)
    {
        auditoriumService.deleteAuditorium(id);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/auditorium/search")
    public ResponseEntity<List<ResAuditoriumDTO>> searchAuditorium(@RequestParam String name)
    {
        List<ResAuditoriumDTO> auditoriums = auditoriumService.getAuditoriumsByName(name);
        return ResponseEntity.ok(auditoriums);
    }
}
