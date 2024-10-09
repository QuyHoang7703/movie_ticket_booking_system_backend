package com.bytecinema.MovieTicketBookingSystem.dto.loginDTO;

import com.bytecinema.MovieTicketBookingSystem.util.constant.GenderEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
@Getter
@Setter
public class ResLoginDTO {
    @JsonProperty("access_token")
    private String accessToken;
    
    private UserLogin userLogin;
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserLogin {
        // private long id;
        // private String email;
        // private String name;

        // private String avatar;
         private long id;

        private String email;

        private String name;

        private String phoneNumber;

        // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        // private LocalDate birthDay;

        private  GenderEnum gender;

        private String avatar;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGetAccount {
        private UserLogin user;
    }
    
}
