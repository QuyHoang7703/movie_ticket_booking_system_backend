package com.bytecinema.MovieTicketBookingSystem.dto.loginDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO { 
    @NotBlank(message = "Email cannot be left blank")
    private String email;
    @NotBlank(message = "Password cannot be left blank")
    private String password;
}
