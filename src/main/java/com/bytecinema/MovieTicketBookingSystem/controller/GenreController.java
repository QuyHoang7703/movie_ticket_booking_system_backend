package com.bytecinema.MovieTicketBookingSystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytecinema.MovieTicketBookingSystem.domain.Genre;
import com.bytecinema.MovieTicketBookingSystem.dto.request.genre.ReqAddGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.genre.ReqUpdateGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.genre.ResGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.role.ResRoleDTO;
import com.bytecinema.MovieTicketBookingSystem.service.GenresService;

import jakarta.validation.Valid;

import java.util.List;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Validated
public class GenreController {
    private final GenresService genresService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/genres")
    public ResponseEntity<ResGenreDTO> createGenre(@Valid @RequestBody ReqAddGenreDTO genre)
    {
        Genre savedGenre = genresService.addGenre(genre.getName(), genre.getDescription());

        // Chuyển đổi từ Genre sang ResGenreDTO
        ResGenreDTO genreDTO = new ResGenreDTO();
        genreDTO.setName(savedGenre.getName());
        genreDTO.setDescription(savedGenre.getDescription());
        genreDTO.setId(savedGenre.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(genreDTO);
    }
    @GetMapping("/genres/{id}")
public ResponseEntity<ResGenreDTO> getGenre(@PathVariable Long id) {
    ResGenreDTO existedGenre = genresService.findGenreById(id);
    return ResponseEntity.ok(existedGenre);
}
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/genres/{id}")
    public ResponseEntity<ResGenreDTO> updateGenre(@PathVariable Long id,@Valid @RequestBody ReqUpdateGenreDTO updateGenreDTO) {
        Genre updatedGenre = genresService.updateGenre(id, updateGenreDTO.getName(), updateGenreDTO.getDescription());

        ResGenreDTO genreDTO = new ResGenreDTO();
        genreDTO.setId(updatedGenre.getId());
        genreDTO.setName(updatedGenre.getName());
        genreDTO.setDescription(updatedGenre.getDescription());

        return ResponseEntity.ok(genreDTO);
    }
    @GetMapping("/genres")
    public ResponseEntity<List<ResGenreDTO>> getAllGenres()
    {
        List<ResGenreDTO> genres = genresService.findAllGenres();

    // Trả về danh sách ResGenreDTO trong phản hồi HTTP
        return ResponseEntity.ok(genres);
    }
}
