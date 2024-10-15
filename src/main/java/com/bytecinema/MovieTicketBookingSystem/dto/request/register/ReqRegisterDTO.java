package com.bytecinema.MovieTicketBookingSystem.dto.request.register;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ReqRegisterDTO {
    @NotBlank(message = "Email cannot be left blank")
    private String email;
    @NotBlank(message = "Password cannot be left blank")
    private String password;
    @NotBlank(message = "Confirm password cannot be left blank")
    private String confirmPassword;
    private long roleId;
}
