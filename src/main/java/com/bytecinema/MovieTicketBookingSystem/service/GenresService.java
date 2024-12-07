package com.bytecinema.MovieTicketBookingSystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytecinema.MovieTicketBookingSystem.domain.Genre;
import com.bytecinema.MovieTicketBookingSystem.domain.Images;
import com.bytecinema.MovieTicketBookingSystem.domain.Movie;
import com.bytecinema.MovieTicketBookingSystem.domain.MovieGenre;
import com.bytecinema.MovieTicketBookingSystem.dto.response.genre.ResGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.GenreRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.MovieGenresRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import java.util.List;
@Service
@RequiredArgsConstructor
public class GenresService {
    private final GenreRepository genreRepository;
    private final MovieGenresRepository movieGenresRepository;
    public Genre addGenre(String name, String description)
    {
        List<Genre> existedGenre = genreRepository.findByNameIgnoreCase(name);
        if (!existedGenre.isEmpty())
        {
            throw new RuntimeException("Tên thể loại đã được sử dụng");
        }
        Genre genre = new Genre();
        genre.setName(name);
        genre.setDescription(description);

        return this.genreRepository.save(genre);
    }

    public ResGenreDTO findGenreById(Long id)
    {
        Genre genre = genreRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));
        
        ResGenreDTO resGenreDTO = new ResGenreDTO();

        resGenreDTO.setDescription(genre.getDescription());
        resGenreDTO.setId(genre.getId());
        resGenreDTO.setName(genre.getName());

        List<MovieGenre> movieGenres = movieGenresRepository.findByGenreId(id);
        List<Movie> movies = movieGenres.stream().map(MovieGenre::getMovie).distinct().collect(Collectors.toList());


        List<ResMovieDTO> movieDTOs = movies.stream()
            .map(movie -> {
                ResMovieDTO dto = new ResMovieDTO();
                dto.setId(movie.getId());
                dto.setName(movie.getName());
                dto.setDuration(movie.getDuration().toString());
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


            resGenreDTO.setFilms(movieDTOs);
            

        
        return resGenreDTO;

    }

    public List<ResGenreDTO> findAllGenres() {
        // Lấy tất cả các thể loại từ repository
        List<Genre> genres = genreRepository.findAll();
    
        // Chuyển đổi danh sách thể loại thành danh sách ResGenreDTO
        List<ResGenreDTO> genreDTOs = genres.stream()
            .map(genre -> {
                ResGenreDTO resGenreDTO = new ResGenreDTO();
                resGenreDTO.setId(genre.getId());
                resGenreDTO.setName(genre.getName());
                resGenreDTO.setDescription(genre.getDescription());
    
                // Lấy danh sách các MovieGenre cho genre hiện tại
                List<MovieGenre> movieGenres = movieGenresRepository.findByGenreId(genre.getId());
                List<Movie> movies = movieGenres.stream()
                    .map(MovieGenre::getMovie)
                    .distinct()
                    .collect(Collectors.toList());
    
                // Chuyển đổi danh sách Movie thành danh sách ResMovieDTO
                List<ResMovieDTO> movieDTOs = movies.stream()
                    .map(movie -> {
                        ResMovieDTO dto = new ResMovieDTO();
                        dto.setId(movie.getId());
                        dto.setName(movie.getName());
                        dto.setDuration(movie.getDuration().toString());
                        dto.setReleaseDay(movie.getReleaseDay());
                        dto.setActors(movie.getActors());
                        dto.setDirector(movie.getDirector());
                        dto.setNation(movie.getNation());
                        dto.setDescription(movie.getDescription());
                        // Chuyển đổi movieGenres sang ResMovieGenreDTO nếu cần
                        List<ResMovieGenreDTO> genreDTOss = movie.getMovieGenres().stream()
                            .map(movieGenre -> {
                                ResMovieGenreDTO genreDTO = new ResMovieGenreDTO();
                                genreDTO.setName(movieGenre.getGenre().getName());
                                genreDTO.setDescription(movieGenre.getGenre().getDescription());
                                return genreDTO;
                            })
                            .collect(Collectors.toList());
    
                        dto.setMovieGenres(genreDTOss); // Thiết lập danh sách thể loại vào DTO
                        List<String> imagePaths = movie.getImages().stream()
                            .map(Images::getImagePath) // Giả định bạn có phương thức getImagePath()
                            .collect(Collectors.toList());
                        dto.setImagePaths(imagePaths);
    
                        return dto;
                    })
                    .collect(Collectors.toList());
    
                resGenreDTO.setFilms(movieDTOs); // Thiết lập danh sách phim vào ResGenreDTO
                return resGenreDTO;
            })
            .collect(Collectors.toList());


        return genreDTOs; // Trả về danh sách ResGenreDTO
    }
    

    @Transactional
    public Genre updateGenre(Long id, String Name, String Description) {
        Genre genre = genreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));
        List<Genre> existedGenres = genreRepository.findByNameIgnoreCase(Name);
        if (!existedGenres.isEmpty() && !genre.getName().equals(Name))
        {
            throw new RuntimeException("Tên thể loại đã được sử dụng");
        }
        if (Name != null)
        {
            genre.setName(Name);
        }
        if (Description != null)
        {
            genre.setDescription(Description);
        }
        

        return genreRepository.save(genre);
    }

    public void deleteGenre(Long id) {
        Genre genre = genreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));
        List<MovieGenre> movieGenres = movieGenresRepository.findByGenreId(id);
        if (!movieGenres.isEmpty())
        {
            throw new RuntimeException("Thể loại đã được sử dụng");
        }
        genreRepository.delete(genre);
    }
}
