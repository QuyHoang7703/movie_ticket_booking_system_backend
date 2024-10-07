package com.bytecinema.MovieTicketBookingSystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String email;
    private String password;
    private String confirmPassword;

}
