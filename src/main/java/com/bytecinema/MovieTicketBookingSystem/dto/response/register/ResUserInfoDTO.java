package com.bytecinema.MovieTicketBookingSystem.dto.response.register;

import com.bytecinema.MovieTicketBookingSystem.util.constant.GenderEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
public class ResUserInfoDTO {
    private UserInfo userInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private long id;

        private String email;

        private String name;

        private String phoneNumber;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        private LocalDate birthDay;

        private  GenderEnum gender;

        private String avatar;

        private String role;



    }


  
}