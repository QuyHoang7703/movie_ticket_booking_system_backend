package com.bytecinema.MovieTicketBookingSystem.dto.request.register;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqVerifyDTO {
    private String email;
    private String otp;
}
