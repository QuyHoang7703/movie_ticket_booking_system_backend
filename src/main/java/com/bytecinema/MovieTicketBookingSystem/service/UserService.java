package com.bytecinema.MovieTicketBookingSystem.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.domain.Role;
import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.dto.ResetPasswordRequest;
import com.bytecinema.MovieTicketBookingSystem.dto.registerDTO.RegisterDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.registerDTO.ResUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.RoleRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.UserRepository;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User handleRegisterUser(RegisterDTO registerDTO) throws IdInValidException{
        Optional<Role> optionalRole = this.roleRepository.findById(registerDTO.getRoleId());
        if(!optionalRole.isPresent()) {
            throw new IdInValidException("Role is invalid");
        }
        Role role = optionalRole.get();
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        user.setRole(role);
        user.setExpirationTime(Instant.now().plus(2, ChronoUnit.MINUTES));
        String otp = this.generateOTP();
        
        String otpDecoded = this.passwordEncoder.encode(otp);
        user.setOtp(otpDecoded);
        //Send OTP to email register
        this.sendVerificationEmail(registerDTO.getEmail(), otp);

        // Add attribute of user
        return this.userRepository.save(user);

    }

    public User fetchUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if(userOptional.isPresent()) {
            return userOptional.get();
        }
        return null;
    }
 

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public User handleGetUserByEmail(String username) {
        Optional<User> optionalUser = this.userRepository.findByEmail(username);
        if(optionalUser.isPresent()){
            return optionalUser.get();
        }
        return null;
               
    }

    public void updateRefreshToken(String refreshToken, String email) {
        User currentUser = this.handleGetUserByEmail(email);
        if(currentUser != null ){
            currentUser.setRefreshToken(refreshToken);
            this.userRepository.save(currentUser);
        }
    }

    public User fetchUserByRefreshTokenAndEmail(String refreshToken, String email) {
        Optional<User> optionalAccount = this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
        if(optionalAccount.isPresent()) {
            return optionalAccount.get();
        }
        return null;
    }
    
    public boolean checkAvailableEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public ResUserInfoDTO convertToResUserInfoDTO(User user){
        ResUserInfoDTO resUser = new ResUserInfoDTO();
        resUser.setId(user.getId());
        resUser.setEmail(user.getEmail());
        resUser.setName(user.getName());
        resUser.setPhoneNumber(user.getPhoneNumber());
        resUser.setBirthDay(user.getBirthDay());
        resUser.setGender(user.getGender());
        resUser.setAvatar(user.getAvatar());
    
        return resUser;
    }

    private String generateOTP() {
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
                throw new IdInValidException("OTP đã hết hạn");
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

    public User handleUpdateUser(User reqUser) {
        long id = reqUser.getId();
        User userUpdate = this.fetchUserById(id);
        if(userUpdate != null) {
            // userUpdate.setPassword(reqUser.getPassword());
            userUpdate.setName(reqUser.getName());
            userUpdate.setPhoneNumber(reqUser.getPhoneNumber());
            userUpdate.setGender(reqUser.getGender());
            userUpdate.setAvatar(reqUser.getAvatar());
            userUpdate.setBirthDay(reqUser.getBirthDay());
            // userUpdate.setLockReason(reqUser.getLockReason());
        }
        return this.userRepository.save(userUpdate);
       
    }

    public void updatePassword(ResetPasswordRequest request) throws IdInValidException{
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new IdInValidException("Mật khẩu và mật khẩu xác nhận không trùng khớp. Vui lòng nhập lại");
        }

        User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IdInValidException("Không tồn tại email này trong hệ thống"));
        if(user.getExpirationTime()==null || Instant.now().isAfter(user.getExpirationTime())){
            throw new IdInValidException("Yêu cầu cập nhập lại mật khẩu của bạn đã hết hiệu lực sử dụng");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRefreshToken(null);
        user.setExpirationTime(null);
        user.setToken(null);
        this.userRepository.save(user);

    }

    

}
