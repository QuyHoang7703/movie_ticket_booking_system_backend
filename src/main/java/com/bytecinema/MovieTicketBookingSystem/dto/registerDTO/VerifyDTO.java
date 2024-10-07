package com.bytecinema.MovieTicketBookingSystem.dto.registerDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyDTO {
    private String email;
    private String otp;
}
