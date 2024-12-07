//package com.bytecinema.MovieTicketBookingSystem.service.redisService;
//
//import com.bytecinema.MovieTicketBookingSystem.dto.response.genre.ResGenreDTO;
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
//public class GenreRedisService {
//    private final RedisTemplate<String, ArrayList<Long>> redisTemplateGenreIds;
//    private final RedisTemplate<String, ResGenreDTO> redisTemplateResGenreDTO;
//    private final ObjectMapper objectMapper;
//
//    public ResGenreDTO getGenreById(long id) {
//        Object rawJson = redisTemplateResGenreDTO.opsForValue().get("genre:" + id);
//        if(rawJson != null){
//            return objectMapper.convertValue(rawJson, ResGenreDTO.class);
//        }
//        return null;
//    }
//
//    public List<ResGenreDTO> getGenres(){
//        Object rawJson = redisTemplateGenreIds.opsForValue().get("genres:all-ids");
//        if(rawJson != null){
//            ArrayList<Long> ids = objectMapper.convertValue(rawJson, new TypeReference<ArrayList<Long>>() {});
//            List<ResGenreDTO> resGenreDTOS = ids.stream()
//                    .map(id-> objectMapper.convertValue(redisTemplateResGenreDTO.opsForValue().get("genre:" +id), ResGenreDTO.class))
//                    .toList();
//            return resGenreDTOS;
//        }
//        return null;
//    }
//
//    public void addMovie(ResGenreDTO resGenreDTO) {
//        if (redisTemplateGenreIds.hasKey("genres:all-ids")) {
//            ArrayList<Long> ids = objectMapper.convertValue(redisTemplateGenreIds.opsForValue().get("genres:all-ids"), new TypeReference<ArrayList<Long>>() {});
//            ids.add(resGenreDTO.getId());
//            redisTemplateGenreIds.opsForValue().set("genres:all-ids", ids);
//            redisTemplateResGenreDTO.opsForValue().set("genre:" + resGenreDTO.getId(), resGenreDTO);
//        }
//    }
//
//    public void updateMovie(ResGenreDTO resGenreDTO) {
//        long id = resGenreDTO.getId();
//        if (redisTemplateGenreIds.hasKey("genre:" + id)) {
//            redisTemplateResGenreDTO.delete("genre:" + id);
//            redisTemplateResGenreDTO.opsForValue().set("genre:" + id, resGenreDTO);
//        }
//    }
//
//    public void deleteMovie(long id) {
//        Object rawJson = redisTemplateGenreIds.opsForValue().get("genres:all-ids");
//        ArrayList<Long> ids = objectMapper.convertValue(rawJson, new TypeReference<ArrayList<Long>>() {});
//        ids.remove(id);
//        redisTemplateGenreIds.opsForValue().set("genres:all-ids", ids);
//        redisTemplateResGenreDTO.delete("genre:" + id);
//    }
//}
