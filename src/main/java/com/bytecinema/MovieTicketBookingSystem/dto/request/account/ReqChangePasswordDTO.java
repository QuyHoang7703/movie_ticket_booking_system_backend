package com.bytecinema.MovieTicketBookingSystem.dto.request.account;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ReqChangePasswordDTO {
    @NotBlank(message = "Token cannot be left blank")
    private String token;
    @NotBlank(message = "Password cannot be left blank")
    private String password;
    @NotBlank(message = "Confirm password cannot be left blank")
    private String confirmPassword;
}

