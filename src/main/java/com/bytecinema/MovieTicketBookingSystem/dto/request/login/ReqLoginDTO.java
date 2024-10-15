package com.bytecinema.MovieTicketBookingSystem.dto.request.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqLoginDTO { 
    @NotBlank(message = "Email cannot be left blank")
    private String email;
    @NotBlank(message = "Password cannot be left blank")
    private String password;
}
