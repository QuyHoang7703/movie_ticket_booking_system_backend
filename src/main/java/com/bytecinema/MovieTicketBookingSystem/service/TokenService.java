package com.bytecinema.MovieTicketBookingSystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.repository.UserRepository;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;

import lombok.RequiredArgsConstructor;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.base.url}")
    private String baseUrl;

    public void sendVerificationEmail(String email, String token) {
        String subject = "Xác thực email của bạn";
        
        // Tạo liên kết chứa token để người dùng có thể nhấp vào
        String resetPasswordLink = baseUrl + "/api/v1/auth/verify-token?token=" + token + "&email=" + email;
    
        String body = "<h1>Xác thực tài khoản của bạn</h1>"
                    + "<p>Chào bạn,</p>"
                    + "<p>Để xác thực tài khoản của bạn, vui lòng nhấp vào liên kết bên dưới:</p>"
                    + "<a href=\"" + resetPasswordLink + "\">Xác thực tài khoản</a>"
                    + "<p>Liên kết này sẽ hết hạn sau 2 phút.</p>"
                    + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>"
                    + "<p>Trân trọng,<br>Đội ngũ hỗ trợ của chúng tôi</p>";
    
        this.emailService.sendEmail(email, subject, body);
    }

    public void createToken(String email) {
        Optional<User> optionalUser = this.userRepository.findByEmail(email);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setOtpExpirationTime(Instant.now().plus(2, ChronoUnit.MINUTES));
            this.userRepository.save(user);
            sendVerificationEmail(email, token);

        }
    }

    public boolean validToken(String email, String token) {
        Optional<User> optionalUser = this.userRepository.findByEmail(email);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            boolean checkToken = user.getToken() != null && user.getToken().equals(token);
            // Instant otpExpirationTime = user.getOtpExpirationTime();
            boolean checkTokenExpired = Instant.now().isAfter(user.getOtpExpirationTime());
            if(checkToken && !checkTokenExpired) {
                user.setToken(null);
                user.setOtpExpirationTime(null);
                this.userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    
}
