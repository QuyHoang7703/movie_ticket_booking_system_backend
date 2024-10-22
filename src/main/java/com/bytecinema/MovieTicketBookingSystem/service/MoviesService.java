package com.bytecinema.MovieTicketBookingSystem.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bytecinema.MovieTicketBookingSystem.domain.Genre;
import com.bytecinema.MovieTicketBookingSystem.domain.Images;
import com.bytecinema.MovieTicketBookingSystem.domain.Movie;
import com.bytecinema.MovieTicketBookingSystem.domain.MovieGenre;
import com.bytecinema.MovieTicketBookingSystem.dto.request.movie.ReqAddMovieDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.screening.ResScreeningDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.GenreRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.ImagesRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.MovieGenresRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.MovieRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import com.bytecinema.MovieTicketBookingSystem.domain.Screening;

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
        List<ResScreeningDTO> screenings = new ArrayList<ResScreeningDTO>();
        resMovieDTO.setScreenings(screenings);
        Movie movie = new Movie();

        movie.setName(addMovieDTO.getName());
        movie.setDescription(addMovieDTO.getDescription());
        movie.setReleaseDay(addMovieDTO.getReleaseDay());
        movie.setDuration(convertToDuration(addMovieDTO.getDuration()));
        movie.setActors(addMovieDTO.getActors());
        movie.setNation(addMovieDTO.getNation());
        movie.setDirector(addMovieDTO.getDirector());

        Movie savedMovie = movieRepository.save(movie);

        resMovieDTO.setId(savedMovie.getId());
        resMovieDTO.setDuration((savedMovie.getDuration().toString()));
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
        
        List<ResMovieDTO> movieDTOs = movies.stream().map(movie -> {
            return convertMovieToResMovieDTO(movie);
        })
        .collect(Collectors.toList());

        return movieDTOs;
    }

    public ResMovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id).orElse(null);
    
        if (movie == null) {
            return null; 
        }
        ResMovieDTO dto = convertMovieToResMovieDTO(movie);
        return dto;
    }
    public List<ResMovieDTO> getMoviesUpcoming()
    {
        Instant currentDate = Instant.now();
        List<Movie> upcomingMovies = movieRepository.findByReleaseDayAfter(currentDate);

        List<ResMovieDTO> movieDTOs = upcomingMovies.stream().map(movie -> {
            return convertMovieToResMovieDTO(movie);
        })
        .collect(Collectors.toList());

        return movieDTOs;
    }

    private Duration convertToDuration(String durationString)
    {
        long minutes = Long.parseLong(durationString);
        return Duration.ofMinutes(minutes);
    }
    private ResMovieDTO convertMovieToResMovieDTO(Movie movie)
    {
        ResMovieDTO dto = new ResMovieDTO();
                dto.setId(movie.getId());
                dto.setName(movie.getName());
                dto.setDuration(movie.getDuration().toString());
                dto.setReleaseDay(movie.getReleaseDay());
                dto.setActors(movie.getActors());
                dto.setDirector(movie.getDirector());
                dto.setNation(movie.getNation());
                dto.setDescription(movie.getDescription());

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

        List<ResScreeningDTO> screeningDTOs = movie.getScreenings().stream().map((screening) -> {
            return convertScreeningToResScreeningDto(screening);
        }).collect(Collectors.toList());

        dto.setScreenings(screeningDTOs);

        return dto;
    }

    private ResScreeningDTO convertScreeningToResScreeningDto(Screening screening)
    {
                    ResScreeningDTO result = new ResScreeningDTO();
                    result.setId(screening.getId());
                    result.setStartTime(screening.getStartTime());
                    result.setEndTime(screening.getEndTime());
                    result.setMovieName(screening.getMovie().getName());
                    result.setAuditoriumName(screening.getAuditorium().getName());
                    result.setMovieId(screening.getMovie().getId());
                    result.setAuditoriumId(screening.getAuditorium().getId());
                    result.setTicketPrice(screening.getTicketPrice());
                    

        return result;
    }

    
}
