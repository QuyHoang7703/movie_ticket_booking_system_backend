package com.bytecinema.MovieTicketBookingSystem.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.repository.UserRepository;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public String generateOTP() {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        
        return String.valueOf(otpValue);
    }

    public void sendVerificationEmail(String email, String otp) {
        String subject = "Email verification";
        String body = "Your verification OTP is: " + otp;
        this.emailService.sendEmail(email, subject, body);
    }

    public void verify(String email, String otp) throws IdInValidException{
        Optional<User> optionalUser = this.userRepository.findByEmail(email);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            boolean isValidOtp = this.passwordEncoder.matches(otp, user.getOtp());
            boolean isOtpExpired = Instant.now().isAfter(user.getExpirationTime());
            if(isValidOtp && !isOtpExpired){
                user.setVerified(true);
                user.setOtp(null);
                user.setExpirationTime(null);
                this.userRepository.save(user);
            }else {
                throw new IdInValidException("OTP không chính xác hoặc đã hết hạn");
            }
        }
    }

    public void resendOtp(String email) {
        String otp = this.generateOTP();
        String otpDecoded = this.passwordEncoder.encode(otp);
        Optional<User> optionalUser = this.userRepository.findByEmail(email);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setOtp(otpDecoded);
            user.setExpirationTime(Instant.now().plus(2, ChronoUnit.MINUTES));
            this.userRepository.save(user);
        }
        this.sendVerificationEmail(email, otp);
    }

    public void sendRequestForgotPassword(String email) throws IdInValidException{
        User user = this.userRepository.findByEmail(email)
                                .orElseThrow(() -> new IdInValidException("Không tồn tại tài khoản này trong hệ thống"));
        if(!user.isVerified()) {
            throw new IdInValidException("Tài khoản đăng ký này chưa được xác thực");
        }

        String otp = this.generateOTP();
        String otpDecoded = this.passwordEncoder.encode(otp);
        user.setOtp(otpDecoded);
        user.setExpirationTime(Instant.now().plus(2, ChronoUnit.MINUTES));
        this.userRepository.save(user);
        this.sendVerificationEmail(email, otp);
        
    }

    public void verify_otp_forgot(String email, String otp) throws IdInValidException{
        Optional<User> optionalUser = this.userRepository.findByEmail(email);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            boolean isValidOtp = this.passwordEncoder.matches(otp, user.getOtp());
            boolean isOtpExpired = Instant.now().isAfter(user.getExpirationTime());
            if(isValidOtp && !isOtpExpired){
                user.setOtp(null);
                // user.setExpirationTime(null);
                this.userRepository.save(user);
            }else {
                throw new IdInValidException("OTP không chính xác hoặc đã hết hạn");
            }
        }
    }
    
}
