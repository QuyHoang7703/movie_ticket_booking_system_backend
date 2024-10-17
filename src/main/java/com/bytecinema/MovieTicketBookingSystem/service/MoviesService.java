package com.bytecinema.MovieTicketBookingSystem.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytecinema.MovieTicketBookingSystem.domain.Genre;
import com.bytecinema.MovieTicketBookingSystem.domain.Images;
import com.bytecinema.MovieTicketBookingSystem.domain.Movie;
import com.bytecinema.MovieTicketBookingSystem.domain.MovieGenre;
import com.bytecinema.MovieTicketBookingSystem.dto.request.movie.ReqAddMovieDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.genre.ResGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.GenreRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.ImagesRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.MovieGenresRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.MovieRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MoviesService {
    private final MovieRepository movieRepository;
    private final ImagesRepository imagesRepository;
    private final MovieGenresRepository movieGenresRepository;
    private final GenreRepository genreRepository;

    @Transactional
    public ResMovieDTO addMovie(ReqAddMovieDTO addMovieDTO)
    {

        ResMovieDTO resMovieDTO = new ResMovieDTO();
        List<ResMovieGenreDTO> resMoviGenreDTO = new ArrayList<ResMovieGenreDTO>();

        Movie movie = new Movie();

        movie.setName(addMovieDTO.getName());
        movie.setDescription(addMovieDTO.getDescription());
        movie.setReleaseDay(addMovieDTO.getReleaseDay());
        movie.setLength(addMovieDTO.getLength());
        movie.setActors(addMovieDTO.getActors());
        movie.setNation(addMovieDTO.getNation());
        movie.setDirector(addMovieDTO.getDirector());

        Movie savedMovie = movieRepository.save(movie);

        resMovieDTO.setId(savedMovie.getId());
        resMovieDTO.setLength(savedMovie.getLength());
        resMovieDTO.setName(savedMovie.getName());
        resMovieDTO.setReleaseDay(savedMovie.getReleaseDay());
        resMovieDTO.setActors(savedMovie.getActors());
        resMovieDTO.setDirector(savedMovie.getDirector());
        resMovieDTO.setNation(savedMovie.getNation());
        resMovieDTO.setDescription(savedMovie.getDescription());

        if (addMovieDTO.getGenreIds() != null && !addMovieDTO.getGenreIds().isEmpty())
        {
            for (Long genreId : addMovieDTO.getGenreIds())
            {
                Genre genre = genreRepository.findById(genreId).orElseThrow(() -> new RuntimeException("Genre not found with id: " + genreId));
                
                MovieGenre movieGenre = new MovieGenre();
                movieGenre.setMovie(savedMovie);
                movieGenre.setGenre(genre);
                movieGenresRepository.save(movieGenre);

                resMoviGenreDTO.add(new ResMovieGenreDTO(genre.getName(), genre.getDescription()));
            
            }
        }

        resMovieDTO.setMovieGenres(resMoviGenreDTO);

        if (addMovieDTO.getImagePaths() != null && !addMovieDTO.getImagePaths().isEmpty())
        {
            for (String imagePath : addMovieDTO.getImagePaths())
            {
                Images image = new Images();
                image.setImagePath(imagePath);
                image.setMovie(savedMovie);

                imagesRepository.save(image);
            }
        }

        return resMovieDTO;
    }

    public List<ResMovieDTO> getAllMovies()
    {
        List<Movie> movies = movieRepository.findAll();

        List<ResMovieDTO> movieDTOs = movies.stream()
            .map(movie -> {
                ResMovieDTO dto = new ResMovieDTO();
                dto.setId(movie.getId());
                dto.setName(movie.getName());
                dto.setLength(movie.getLength());
                dto.setReleaseDay(movie.getReleaseDay());
                dto.setActors(movie.getActors());
                dto.setDirector(movie.getDirector());
                dto.setNation(movie.getNation());
                dto.setDescription(movie.getDescription());
                
                // Chuyển đổi movieGenres sang ResMovieGenreDTO nếu cần
                List<ResMovieGenreDTO> genreDTOs = movie.getMovieGenres().stream()
                        .map(movieGenre -> {
                            ResMovieGenreDTO genreDTO = new ResMovieGenreDTO();
                            genreDTO.setName(movieGenre.getGenre().getName());
                            genreDTO.setDescription(movieGenre.getGenre().getDescription());
                            return genreDTO;
                        })
                        .collect(Collectors.toList());

                dto.setMovieGenres(genreDTOs); // Thiết lập danh sách thể loại vào DTO
                List<String> imagePaths = movie.getImages().stream()
                    .map(Images::getImagePath) // Giả định bạn có phương thức getImagePath()
                    .collect(Collectors.toList());
            dto.setImagePaths(imagePaths); 
                return dto;
            })
            .collect(Collectors.toList());

        return movieDTOs;
    }

    public ResMovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id).orElse(null); // Tìm kiếm phim theo ID
    
        if (movie == null) {
            return null; // Trả về null nếu không tìm thấy phim
        }
    
        // Chuyển đổi Movie sang ResMovieDTO
        ResMovieDTO dto = new ResMovieDTO();
        dto.setId(movie.getId());
        dto.setName(movie.getName());
        dto.setLength(movie.getLength());
        dto.setReleaseDay(movie.getReleaseDay());
        dto.setActors(movie.getActors());
                dto.setDirector(movie.getDirector());
                dto.setNation(movie.getNation());
                dto.setDescription(movie.getDescription());
    
        // Chuyển đổi movieGenres sang ResMovieGenreDTO
        List<ResMovieGenreDTO> genreDTOs = movie.getMovieGenres().stream()
                .map(movieGenre -> {
                    ResMovieGenreDTO genreDTO = new ResMovieGenreDTO();
                    
                    genreDTO.setName(movieGenre.getGenre().getName());
                    genreDTO.setDescription(movieGenre.getGenre().getDescription());
                    return genreDTO;
                })
                .collect(Collectors.toList());
    
        dto.setMovieGenres(genreDTOs); // Thiết lập danh sách thể loại vào DTO
    
        // Chuyển đổi images sang imagePaths
        List<String> imagePaths = movie.getImages().stream()
                .map(Images::getImagePath) // Giả định bạn có phương thức getImagePath()
                .collect(Collectors.toList());
        dto.setImagePaths(imagePaths); // Thiết lập danh sách đường dẫn ảnh vào DTO
    
        return dto;
    }
}
