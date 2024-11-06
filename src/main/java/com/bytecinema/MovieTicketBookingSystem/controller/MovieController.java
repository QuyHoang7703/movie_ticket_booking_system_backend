package com.bytecinema.MovieTicketBookingSystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bytecinema.MovieTicketBookingSystem.domain.Movie;
import com.bytecinema.MovieTicketBookingSystem.dto.request.movie.ReqAddMovieDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.genre.ResGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieDTO;
import com.bytecinema.MovieTicketBookingSystem.service.MoviesService;
import com.bytecinema.MovieTicketBookingSystem.service.S3Service;

import jakarta.validation.Valid;

import java.util.List;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Validated
public class MovieController {
    private final MoviesService moviesService;
    private final S3Service s3Service;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value="/movies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMovie(
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @Valid @RequestPart("movie-info") ReqAddMovieDTO addMovieDTO) {

        List<String> pathImages = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Kiểm tra định dạng file
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                String contentType = imageFile.getContentType();
                if (contentType == null || 
                    !(contentType.equals(MediaType.IMAGE_JPEG_VALUE) || 
                      contentType.equals(MediaType.IMAGE_PNG_VALUE)) || contentType.equals("image/jpg")) {

                    errors.add("File " + imageFile.getOriginalFilename() + " phải có định dạng jpg, jpeg, hoặc png.");
                } else {
                    // Upload file nếu định dạng hợp lệ
                    String file = s3Service.uploadFile(imageFile);
                    pathImages.add(file);
                }
            }
        }

        // Trả về lỗi nếu có file không hợp lệ
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        // Set danh sách ảnh đã upload vào đối tượng addMovieDTO
        addMovieDTO.setImagePaths(pathImages);
        ResMovieDTO savedMovie = moviesService.addMovie(addMovieDTO);

        // Thiết lập đường dẫn ảnh cho đối tượng phản hồi
        savedMovie.setImagePaths(pathImages);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);
    }
    @GetMapping("/movies")
    public ResponseEntity<List<ResMovieDTO>> getAllMovies()
    {
        List<ResMovieDTO> movies = moviesService.getAllMovies();
        return ResponseEntity.status(HttpStatus.OK).body(movies);
    }

    @GetMapping("/movies/{id}")
public ResponseEntity<ResMovieDTO> getMovieById(@PathVariable Long id) {
    ResMovieDTO movieDTO = moviesService.getMovieById(id);
    
    if (movieDTO == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Trả về 404 nếu không tìm thấy phim
    }
    
    return ResponseEntity.ok(movieDTO); // Trả về phim nếu tìm thấy
}

    @GetMapping("/movies/upcoming")
    public ResponseEntity<List<ResMovieDTO>> getMoviesUpcoming()
    {
        List<ResMovieDTO> movies = moviesService.getMoviesUpcoming();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/movies/search")
    public ResponseEntity<List<ResMovieDTO>> searchMovie(@RequestParam String name)
    {
        List<ResMovieDTO> movies = moviesService.getMoviesByName(name);
        return ResponseEntity.ok(movies);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/movies/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMovie(
        @PathVariable Long id,
        @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
        @Valid @RequestPart("movie-info") ReqAddMovieDTO updateMovieDTO) {

        // Danh sách để lưu trữ đường dẫn hình ảnh đã tải lên
        List<String> pathImages = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Kiểm tra định dạng file
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                String contentType = imageFile.getContentType();
                if (contentType == null ||
                    !(contentType.equals(MediaType.IMAGE_JPEG_VALUE) ||
                      contentType.equals(MediaType.IMAGE_PNG_VALUE) || contentType.equals("image/jpg"))) {

                    errors.add("File " + imageFile.getOriginalFilename() + " phải có định dạng jpg, jpeg, hoặc png.");
                } else {
                    // Upload file nếu định dạng hợp lệ
                    String file = s3Service.uploadFile(imageFile);
                    pathImages.add(file);
                }
            }
        }

        // Trả về lỗi nếu có file không hợp lệ
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        updateMovieDTO.setImagePaths(pathImages);
        // Gọi service để cập nhật phim
        ResMovieDTO updatedMovie = moviesService.updateMovie(id, updateMovieDTO);

        // Trả về kết quả
        return ResponseEntity.ok(updatedMovie);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id)
    {
        moviesService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
