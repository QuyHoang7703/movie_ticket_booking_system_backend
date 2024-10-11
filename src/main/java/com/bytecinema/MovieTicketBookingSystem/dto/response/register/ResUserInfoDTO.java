package com.bytecinema.MovieTicketBookingSystem.dto.response.register;

import com.bytecinema.MovieTicketBookingSystem.util.constant.GenderEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class ResUserInfoDTO {
     private long id;

    private String email;

    private String name;

    private String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate birthDay;

    private  GenderEnum gender;

    private String avatar;

  
}