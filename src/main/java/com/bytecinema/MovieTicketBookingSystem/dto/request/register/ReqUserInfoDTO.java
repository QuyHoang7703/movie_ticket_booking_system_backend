package com.bytecinema.MovieTicketBookingSystem.dto.request.register;

import com.bytecinema.MovieTicketBookingSystem.util.constant.GenderEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

import lombok.*;

@Data
public class ReqUserInfoDTO {
    private long id;

    private String email;

    private String name;

    private String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate birthDay;

    private  GenderEnum gender;

    private String avatar;



}
