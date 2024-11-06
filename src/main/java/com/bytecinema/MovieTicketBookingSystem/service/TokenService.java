package com.bytecinema.MovieTicketBookingSystem.service;


import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.repository.UserRepository;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TokenService {
    private final EmailService emailService;
    private final UserRepository userRepository;

    public void createToken(String email) throws IdInValidException {
        Optional<User> optionalAccount = this.userRepository.findByEmail(email);
        if(optionalAccount.isPresent()) {
            User user = optionalAccount.get();
            if((user.isVerified())==false) {
                throw new IdInValidException("Tài khoản này đã bị khóa");
            }

            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setExpirationTime(Instant.now().plus(3, ChronoUnit.MINUTES));

            this.userRepository.save(user);
            this.sendRequestForgotPassword(email, user.getName(), token);
            System.out.println(">>>>>>> Token: " + token);
        }else{
            throw new IdInValidException("Email này chưa được đăng ký trong hệ thống");
        }
    }


    public boolean isValidToken(String token) {
        Optional<User> optionalAccount = this.userRepository.findByToken(token);
        if(optionalAccount.isPresent()) {
            User user = optionalAccount.get();
            // boolean checkToken = user.getToken() != null && user.getToken().equals(token);
            boolean checkTokenExpired = Instant.now().isAfter(user.getExpirationTime());
            if(!checkTokenExpired) {
                // user.setToken(null);
                // user.setRefreshToken(null);
                this.userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    public void sendRequestForgotPassword(String email, String name, String token) {
        String subject = "Yêu cầu đặt lại mật khẩu";

        // Tạo liên kết chứa token để người dùng nhấn vào => chuyển đến fe xử lý
        String resetPasswordLink = "http://localhost:3000/reset-password?token=" + token;

        // Tạo nội dung email từ template HTML
        Context context = new Context();
        context.setVariable("userName", name);
        context.setVariable("resetPasswordLink", resetPasswordLink);

        this.emailService.sendEmail(email, subject, "reset_password_email", context);

    }
    
}
