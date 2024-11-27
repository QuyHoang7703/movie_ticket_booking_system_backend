package com.bytecinema.MovieTicketBookingSystem.service;
import com.bytecinema.MovieTicketBookingSystem.service.redisService.MovieRedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytecinema.MovieTicketBookingSystem.domain.Booking;
import com.bytecinema.MovieTicketBookingSystem.domain.Genre;
import com.bytecinema.MovieTicketBookingSystem.domain.Images;
import com.bytecinema.MovieTicketBookingSystem.domain.Movie;
import com.bytecinema.MovieTicketBookingSystem.domain.MovieGenre;
import com.bytecinema.MovieTicketBookingSystem.dto.request.movie.ReqAddMovieDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieAllRevenueDateDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieGenreDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieRevenueDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieRevenueDateDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.screening.ResScreeningDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.GenreRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.ImagesRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.MovieGenresRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.MovieRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import com.bytecinema.MovieTicketBookingSystem.domain.Screening;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoviesService {
    private final MovieRepository movieRepository;
    private final ImagesRepository imagesRepository;
    private final MovieGenresRepository movieGenresRepository;
    private final GenreRepository genreRepository;
    private final S3Service s3Service;
    private final RedisTemplate<String, ResMovieDTO> redisTemplateResMovieDTO;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, List<Long>> redisTemplateMovieIds;
    private final MovieRedisService movieRedisService;
    @Transactional
    public ResMovieDTO addMovie(ReqAddMovieDTO addMovieDTO)
    {
        List<Movie> existedMovies = movieRepository.findByNameIgnoreCase(addMovieDTO.getName());
        if (!existedMovies.isEmpty())
        {
            throw new RuntimeException("Tên phim đã tồn tại, vui lòng chọn tên khác");
        }
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
        movie.setPathTrailer(addMovieDTO.getPathTrailer());
        movie.setLanguage(addMovieDTO.getLanguage());

        Movie savedMovie = movieRepository.save(movie);

        resMovieDTO.setId(savedMovie.getId());
        resMovieDTO.setDuration((savedMovie.getDuration().toString()));
        resMovieDTO.setName(savedMovie.getName());
        resMovieDTO.setReleaseDay(savedMovie.getReleaseDay());
        resMovieDTO.setActors(savedMovie.getActors());
        resMovieDTO.setDirector(savedMovie.getDirector());
        resMovieDTO.setNation(savedMovie.getNation());
        resMovieDTO.setDescription(savedMovie.getDescription());
        resMovieDTO.setPathTrailer(savedMovie.getPathTrailer());
        resMovieDTO.setLanguage(savedMovie.getLanguage());

        if (addMovieDTO.getGenreIds() != null && !addMovieDTO.getGenreIds().isEmpty())
        {
            for (Long genreId : addMovieDTO.getGenreIds())
            {
                Genre genre = genreRepository.findById(genreId).orElseThrow(() -> new RuntimeException("Genre not found with id: " + genreId));
                
                MovieGenre movieGenre = new MovieGenre();
                movieGenre.setMovie(savedMovie);
                movieGenre.setGenre(genre);
                
                movieGenresRepository.save(movieGenre);

                resMoviGenreDTO.add(new ResMovieGenreDTO(genre.getName(), genre.getDescription(), genre.getId()));
            
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

        // Thêm images vào
        List<String> imagePaths = addMovieDTO.getImagePaths();
        resMovieDTO.setImagePaths(imagePaths);

        // Thêm vào Redis
        movieRedisService.addMovie(resMovieDTO);

        return resMovieDTO;
    }


    @Transactional
    public ResMovieDTO updateMovie(Long id, ReqAddMovieDTO updateMovieDTO) {
        // Kiểm tra sự tồn tại của phim
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        // Code này check xem phim đã chiếu chưa
        // boolean isScreeningExist = movie.getScreenings().stream().anyMatch(screening -> screening.getStartTime().isBefore(Instant.now()));

        boolean isScreeningExist = movie.getScreenings().size() > 0;
        if (isScreeningExist) {
            throw new RuntimeException("Không thể cập nhật phim đã chiếu");
        }

        List<Movie> existedMovies = movieRepository.findByNameIgnoreCase(updateMovieDTO.getName());
        if (!existedMovies.isEmpty() && !movie.getName().equals(updateMovieDTO.getName()))
        {
            throw new RuntimeException("Không thể cập nhật 1 phim đã tồn tại");
        }

        // Cập nhật thông tin phim
        movie.setName(updateMovieDTO.getName());
        movie.setDescription(updateMovieDTO.getDescription());
        movie.setReleaseDay(updateMovieDTO.getReleaseDay());
        movie.setDuration(convertToDuration(updateMovieDTO.getDuration()));
        movie.setActors(updateMovieDTO.getActors());
        movie.setNation(updateMovieDTO.getNation());
        movie.setDirector(updateMovieDTO.getDirector());
        movie.setPathTrailer(updateMovieDTO.getPathTrailer());
        movie.setLanguage(updateMovieDTO.getLanguage());

        // Lưu phim đã cập nhật
        Movie savedMovie = movieRepository.save(movie);

        List<ResMovieGenreDTO> resMoviGenreDTO = new ArrayList<>();
        if (updateMovieDTO.getGenreIds() != null && !updateMovieDTO.getGenreIds().isEmpty()) {
            // Cập nhật thể loại
            movieGenresRepository.deleteByMovieId(id); // Xóa các thể loại cũ
            for (Long genreId : updateMovieDTO.getGenreIds()) {
                Genre genre = genreRepository.findById(genreId)
                    .orElseThrow(() -> new RuntimeException("Genre not found with id: " + genreId));
                    
                MovieGenre movieGenre = new MovieGenre();
                movieGenre.setMovie(savedMovie);
                movieGenre.setGenre(genre);
                movieGenresRepository.save(movieGenre);

                resMoviGenreDTO.add(new ResMovieGenreDTO(genre.getName(), genre.getDescription(), genre.getId()));
            }
        }else{
            log.info("genre null");
        }

        // Cập nhật hình ảnh
        log.info("New Image path: "+updateMovieDTO.getImagePaths());
        if (updateMovieDTO.getImagePaths() != null && !updateMovieDTO.getImagePaths().isEmpty()) {

            imagesRepository.deleteByMovieId(id); // Xóa hình ảnh cũ
            // Xóa ảnh trên s3
            List<String> urlImages = movie.getImages().stream().map(image -> image.getImagePath())
                    .toList();
            if(urlImages != null && !urlImages.isEmpty()){
                log.info("Deleted images to delete from S3.");
                this.s3Service.deleteFiles(urlImages);
            }else {
                // Log nếu không có ảnh nào để xóa
                log.info("No images to delete from S3.");
            }
            List<Images> newImages = new ArrayList<>();
            for (String imagePath : updateMovieDTO.getImagePaths()) {
                Images image = new Images();
                image.setImagePath(imagePath);
                image.setMovie(savedMovie);
                newImages.add(image);

            }
            imagesRepository.saveAll(newImages);
            savedMovie.setImages(newImages);

        }else{
            log.info("Image path: null");
        }

        ResMovieDTO res = convertMovieToResMovieDTO(savedMovie);

        //Update trong redis
        movieRedisService.updateMovie(res);

        // Trả về thông tin phim đã cập nhật
        return res;
    }

    @Transactional
    public void deleteMovie(Long id) {
        // Kiểm tra sự tồn tại của phim
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        // Code này check xem phim đã chiếu chưa
        // boolean isScreeningExist = movie.getScreenings().stream().anyMatch(screening -> screening.getStartTime().isBefore(Instant.now()));

        boolean isScreeningExist = movie.getScreenings().size() > 0;
        if (isScreeningExist) {
            throw new RuntimeException("Cannot delete movie that has been screened");
        }

        List<MovieGenre> movieGenres = movie.getMovieGenres();
        if (movieGenres != null && !movieGenres.isEmpty()) {
            movieGenresRepository.deleteAll(movieGenres);
        }

        List<Images> images = movie.getImages();
        if (images != null && !images.isEmpty()) {
            imagesRepository.deleteAll(images);
        }

        movieRepository.delete(movie);

        // Xóa image trên s3
        List<String> urlImages = movie.getImages().stream().map(image -> image.getImagePath())
                .toList();
        if(urlImages != null && !urlImages.isEmpty()){
            this.s3Service.deleteFiles(urlImages);
        }

        // Xóa movie trong redis
        movieRedisService.deleteMovie(id);
    }

//    @Cacheable(cacheNames = "movies", key = "'all-movies'")
    public List<ResMovieDTO> getAllMovies()
    {
        // Kiểm tra trong redis
        List<ResMovieDTO> resMovieDTOsInRedis = movieRedisService.getAllMovies();
        if(resMovieDTOsInRedis != null){
            return resMovieDTOsInRedis;
        }

        // Lấy data từ database
        List<Movie> movies = movieRepository.findAll();

        // Lưu danh sách IDs vào Redis
        List<Long> ids = movies.stream().map(Movie::getId).toList();
        redisTemplateMovieIds.opsForValue().set("movies:all-ids", ids);

        // Lưu từng movie vào Redis và convert thành DTO
        List<ResMovieDTO> movieDTOs = movies.stream()
                .map(movie -> {
                    ResMovieDTO dto = convertMovieToResMovieDTO(movie);
                    redisTemplateResMovieDTO.opsForValue().set("movie:" + movie.getId(), dto);
                    return dto;
                }).toList();

        return movieDTOs;
    }

    public ResMovieDTO getMovieById(Long id) {
        // Kiểm tra data đã có trong redis ?
        ResMovieDTO resMovieDTO = movieRedisService.getMovieById(id);
        if(resMovieDTO != null){
            return resMovieDTO;
        }
        // Lấy data từ database
        Movie movie = movieRepository.findById(id).orElse(null);
    
        if (movie == null) {
            return null; 
        }
        ResMovieDTO dto = convertMovieToResMovieDTO(movie);
        // Lưu data vào redis
        redisTemplateResMovieDTO.opsForValue().set("movie:" + id, dto);

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

    public List<ResMovieDTO> getMoviesByName(String name)
    {
        List<Movie> movies = movieRepository.findByNameStartingWithIgnoreCase(name);

        List<ResMovieDTO> movieDTOs = movies.stream().map(movie -> {
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
                dto.setPathTrailer(movie.getPathTrailer());
                dto.setLanguage(movie.getLanguage());

                List<ResMovieGenreDTO> genreDTOs = movie.getMovieGenres().stream()
                .map(movieGenre -> {
                    ResMovieGenreDTO genreDTO = new ResMovieGenreDTO();
                    genreDTO.setName(movieGenre.getGenre().getName());
                    genreDTO.setDescription(movieGenre.getGenre().getDescription());
                    genreDTO.setId(movieGenre.getId());
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

    public ResMovieRevenueDTO getMovieRevenue(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        // Tính tổng số vé đã bán và doanh thu
        long totalTicketsSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalTicket = 0;

        for (Screening screening : movie.getScreenings()) {
            var auditorium = screening.getAuditorium();
            totalTicket += auditorium.getCapacity();
            for (Booking booking : screening.getBookings()) {
                int ticketsCount = booking.getSeats().size();
                totalTicketsSold += ticketsCount;
                totalRevenue = totalRevenue.add(screening.getTicketPrice().multiply(BigDecimal.valueOf(ticketsCount)));
            }
        }


        var response = new ResMovieRevenueDTO();
        response.setMovieId(movieId);
        response.setMovieName(movie.getName());
        response.setTotalRevenue(totalRevenue);
        response.setTotalTicketsSold(totalTicketsSold);
        response.setTotalTicket(totalTicket);

        return response;
    }
    public List<ResMovieRevenueDTO> getMoviesRevenue() {
        List<Movie> movies = movieRepository.findAll();
        List<ResMovieRevenueDTO> revenueDTOList = new ArrayList<>();
    
        for (Movie movie : movies) {
            long totalTicketsSold = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            long totalTicket = 0;
            for (Screening screening : movie.getScreenings()) {
                var auditorium = screening.getAuditorium();
                totalTicket += auditorium.getCapacity();
                for (Booking booking : screening.getBookings()) {
                    int ticketsCount = booking.getSeats().size();
                    totalTicketsSold += ticketsCount;
                    totalRevenue = totalRevenue.add(screening.getTicketPrice().multiply(BigDecimal.valueOf(ticketsCount)));
                }
            }
            var response = new ResMovieRevenueDTO();
            response.setMovieId(movie.getId());
            response.setMovieName(movie.getName());
            response.setTotalRevenue(totalRevenue);
            response.setTotalTicketsSold(totalTicketsSold);
            response.setTotalTicket(totalTicket);

            revenueDTOList.add(response);
        }
    
        return revenueDTOList;
    }
    public List<ResMovieRevenueDateDTO> getMoviesRevenueByIdAndMonth(long movieId, Instant date) {
        // Xác định tháng và năm từ `Instant`
        LocalDate localDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
    
        // Lấy danh sách tất cả các ngày trong tháng
        List<LocalDate> allDates = getAllDatesInMonth(year, month);
    
        List<ResMovieRevenueDateDTO> result = new ArrayList<>();
        for (LocalDate day : allDates) {
            // Tính tổng số vé đã bán và doanh thu trong ngày đó
            BigDecimal totalRevenue = BigDecimal.ZERO;
            long totalTicketsSold = 0;
            long totalTicket = 0;
    
            Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
    
            for (Screening screening : movie.getScreenings()) {
                if (screening.getStartTime().atZone(ZoneId.systemDefault()).toLocalDate().equals(day)) {
                    var auditorium = screening.getAuditorium();
                    totalTicket += auditorium.getCapacity();
    
                    for (Booking booking : screening.getBookings()) {
                        int ticketsCount = booking.getSeats().size();
                        totalTicketsSold += ticketsCount;
                        totalRevenue = totalRevenue.add(screening.getTicketPrice().multiply(BigDecimal.valueOf(ticketsCount)));
                    }
                }
            }
    
            // Tạo DTO cho ngày hiện tại
            var response = new ResMovieRevenueDateDTO();
            response.setMovieId(movieId);
            response.setMovieName(movie.getName());
            response.setTotalRevenue(totalRevenue);
            response.setTotalTicketsSold(totalTicketsSold);
            response.setTotalTicket(totalTicket);
            response.setTime(day.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
    
            result.add(response);
        }
    
        return result;
    }
    public List<ResMovieRevenueDateDTO> getMoviesRevenueByIdAndYear(long movieId, Instant date) {
        // Lấy năm từ `Instant`
        LocalDate localDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
    
        // Lấy danh sách tất cả các tháng trong năm
        List<YearMonth> allMonths = getAllMonthsInYear(year);
    
        List<ResMovieRevenueDateDTO> result = new ArrayList<>();
        for (YearMonth yearMonth : allMonths) {
            // Tính tổng số vé đã bán và doanh thu trong tháng đó
            BigDecimal totalRevenue = BigDecimal.ZERO;
            long totalTicketsSold = 0;
            long totalTicket = 0;
    
            Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
    
            for (Screening screening : movie.getScreenings()) {
                LocalDate screeningDate = screening.getStartTime().atZone(ZoneId.systemDefault()).toLocalDate();
                YearMonth screeningMonth = YearMonth.from(screeningDate);
    
                if (screeningMonth.equals(yearMonth)) {
                    var auditorium = screening.getAuditorium();
                    totalTicket += auditorium.getCapacity();
    
                    for (Booking booking : screening.getBookings()) {
                        int ticketsCount = booking.getSeats().size();
                        totalTicketsSold += ticketsCount;
                        totalRevenue = totalRevenue.add(screening.getTicketPrice().multiply(BigDecimal.valueOf(ticketsCount)));
                    }
                }
            }
    
            // Tạo DTO cho tháng hiện tại
            var response = new ResMovieRevenueDateDTO();
            response.setMovieId(movieId);
            response.setMovieName(movie.getName());
            response.setTotalRevenue(totalRevenue);
            response.setTotalTicketsSold(totalTicketsSold);
            response.setTotalTicket(totalTicket);
            //response.setTime(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()); // Lấy ngày đầu tháng
            response.setTime(
            yearMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
        );
            result.add(response);
        }
    
        return result;
    }
    public List<ResMovieAllRevenueDateDTO> getMoviesRevenuesByYear(Instant date) {
        // Lấy năm từ `Instant`
        LocalDate localDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
    
        // Lấy danh sách tất cả các tháng trong năm
        List<YearMonth> allMonths = getAllMonthsInYear(year);
    
        List<ResMovieAllRevenueDateDTO> result = new ArrayList<>();
    
        for (YearMonth yearMonth : allMonths) {
            int month = yearMonth.getMonthValue();
            BigDecimal totalRevenueAll = BigDecimal.ZERO;
            long totalTicketsSoldAll = 0;
            long totalTicketAll = 0;
            List<Movie> movies = movieRepository.findAll(); // Lấy toàn bộ phim
            for (Movie movie : movies) {
                BigDecimal totalRevenue = BigDecimal.ZERO;
                long totalTicketsSold = 0;
                long totalTicket = 0;
                for (Screening screening : movie.getScreenings()) {
                    LocalDate screeningDate = screening.getStartTime().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (screeningDate.getYear() == year && screeningDate.getMonthValue() == month) {
                        var auditorium = screening.getAuditorium();
                        totalTicket += auditorium.getCapacity();
    
                        for (Booking booking : screening.getBookings()) {
                            int ticketsCount = booking.getSeats().size();
                            totalTicketsSold += ticketsCount;
                            totalRevenue = totalRevenue.add(screening.getTicketPrice().multiply(BigDecimal.valueOf(ticketsCount)));
                        }
                    }
                }
                totalRevenueAll = totalRevenueAll.add(totalRevenue);
                totalTicketsSoldAll += totalTicketsSold;
                totalTicketAll += totalTicket;
                
            }
            var response = new ResMovieAllRevenueDateDTO();
                response.setTotalRevenue(totalRevenueAll);
                response.setTotalTicketsSold(totalTicketsSoldAll);
                response.setTotalTicket(totalTicketAll);
                response.setTime(yearMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
    
                result.add(response);
        }
    
        return result;
    }
    public List<ResMovieAllRevenueDateDTO> getMoviesRevenuesByMonth(Instant date) {
        // Xác định tháng và năm từ `Instant`
        LocalDate localDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
    
        // Lấy danh sách tất cả các ngày trong tháng
        List<LocalDate> allDates = getAllDatesInMonth(year, month);
    
        List<ResMovieAllRevenueDateDTO> result = new ArrayList<>();
    
        for (LocalDate day : allDates) {
            BigDecimal totalRevenueAll = BigDecimal.ZERO;
            long totalTicketsSoldAll = 0;
            long totalTicketAll = 0;
    
            List<Movie> movies = movieRepository.findAll(); // Lấy toàn bộ phim
            for (Movie movie : movies) {
                BigDecimal totalRevenue = BigDecimal.ZERO;
                long totalTicketsSold = 0;
                long totalTicket = 0;
    
                for (Screening screening : movie.getScreenings()) {
                    LocalDate screeningDate = screening.getStartTime().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (screeningDate.equals(day)) {
                        var auditorium = screening.getAuditorium();
                        totalTicket += auditorium.getCapacity();
    
                        for (Booking booking : screening.getBookings()) {
                            int ticketsCount = booking.getSeats().size();
                            totalTicketsSold += ticketsCount;
                            totalRevenue = totalRevenue.add(screening.getTicketPrice().multiply(BigDecimal.valueOf(ticketsCount)));
                        }
                    }
                }
    
                // Cộng dồn vào tổng số doanh thu, vé đã bán và tổng số vé của tất cả các phim
                totalRevenueAll = totalRevenueAll.add(totalRevenue);
                totalTicketsSoldAll += totalTicketsSold;
                totalTicketAll += totalTicket;
            }
    
            // Tạo DTO cho ngày hiện tại
            var response = new ResMovieAllRevenueDateDTO();
            response.setTotalRevenue(totalRevenueAll);
            response.setTotalTicketsSold(totalTicketsSoldAll);
            response.setTotalTicket(totalTicketAll);
            response.setTime(day.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
    
            result.add(response);
        }
    
        return result;
    }
    private List<LocalDate> getAllDatesInMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth(); // Lấy số ngày trong tháng
    
        List<LocalDate> dates = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            dates.add(LocalDate.of(year, month, day));
        }
        return dates;
    }
    private List<YearMonth> getAllMonthsInYear(int year) {
        List<YearMonth> months = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            months.add(YearMonth.of(year, month));
        }
        return months;
    }

}
