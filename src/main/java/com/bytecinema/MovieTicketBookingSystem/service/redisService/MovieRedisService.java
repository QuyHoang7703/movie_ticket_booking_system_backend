//package com.bytecinema.MovieTicketBookingSystem.service.redisService;
//
//import com.bytecinema.MovieTicketBookingSystem.dto.response.movie.ResMovieDTO;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class MovieRedisService {
//    private final RedisTemplate<String, ArrayList<Long>> redisTemplateMovieIds;
//    private final RedisTemplate<String, ResMovieDTO> redisTemplateResMovieDTO;
//    private final ObjectMapper objectMapper;
//
//    public ResMovieDTO getMovieById(long id) {
//        Object rawJson = redisTemplateResMovieDTO.opsForValue().get("movie:" + id);
//        if(rawJson != null){
//            return objectMapper.convertValue(rawJson, ResMovieDTO.class);
//        }
//        return null;
//    }
//
//    public List<ResMovieDTO> getAllMovies() {
//        Object rawJson = redisTemplateMovieIds.opsForValue().get("movies:all-ids");
//        log.info("Get ids: " + rawJson);
//        // Kiểm tra trong redis đã có data chưa
//        if(rawJson != null){
//            ArrayList<Long> ids = objectMapper.convertValue(rawJson, new TypeReference<ArrayList<Long>>() {});
//            // Lấy dữ liệu từng movie từ Redis
//            if (ids.stream().anyMatch(id -> this.getMovieById(id) == null)) {
//                return null; // Trả về null nếu có giá trị null
//            }
//
//            List<ResMovieDTO> res = ids.stream()
//                    .map(id ->this.getMovieById(id))
//                    .toList();
//
//            return res;
//        }
//
//        return null;
//    }
//
//
//    public void addMovie(ResMovieDTO resMovieDTO) {
//        if (redisTemplateMovieIds.hasKey("movies:all-ids")) {
//            ArrayList<Long> ids = objectMapper.convertValue(redisTemplateMovieIds.opsForValue().get("movies:all-ids"), new TypeReference<ArrayList<Long>>() {});
//            ids.add(resMovieDTO.getId());
//            log.info("Id of movies: " + ids);
//            redisTemplateMovieIds.opsForValue().set("movies:all-ids", ids);
//            redisTemplateResMovieDTO.opsForValue().set("movie:" + resMovieDTO.getId(), resMovieDTO);
//        }
//    }
//
//    public void updateMovie(ResMovieDTO resMovieDTO) {
//        long id = resMovieDTO.getId();
//        if (redisTemplateMovieIds.hasKey("movie:" + id)) {
//            redisTemplateResMovieDTO.delete("movie:" + id);
//            redisTemplateResMovieDTO.opsForValue().set("movie:" + id, resMovieDTO);
//        }
//    }
//
//    public void deleteMovie(long id) {
//        Object rawJson = redisTemplateMovieIds.opsForValue().get("movies:all-ids");
//        if(rawJson != null){
//            ArrayList<Long> ids = objectMapper.convertValue(rawJson, new TypeReference<ArrayList<Long>>() {});
//            for(Long item: ids) {
//                if(item == id) {
//                    ids.remove(id);
//                    redisTemplateMovieIds.opsForValue().set("movies:all-ids", ids);
//                    redisTemplateResMovieDTO.delete("movie:" + id);
//                    break;
//                }
//            }
//
//        }
//
//    }
//
//
//
//
//}
