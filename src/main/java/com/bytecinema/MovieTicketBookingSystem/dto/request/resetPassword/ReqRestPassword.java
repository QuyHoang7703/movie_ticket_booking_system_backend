package com.bytecinema.MovieTicketBookingSystem.dto.request.resetPassword;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRestPassword {
    private String email;
    private String password;
    private String confirmPassword;

}
