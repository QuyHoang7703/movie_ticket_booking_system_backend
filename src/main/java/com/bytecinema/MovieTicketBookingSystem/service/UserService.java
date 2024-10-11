package com.bytecinema.MovieTicketBookingSystem.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.domain.Role;
import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqRegisterDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.resetPassword.ReqRestPassword;
import com.bytecinema.MovieTicketBookingSystem.dto.response.register.ResUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.RoleRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.UserRepository;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;
   
    public User handleRegisterUser(ReqRegisterDTO registerDTO) throws IdInValidException{
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
        String otp = this.otpService.generateOTP();
        
        String otpDecoded = this.passwordEncoder.encode(otp);
        user.setOtp(otpDecoded);
        //Send OTP to email register
        this.otpService.sendVerificationEmail(registerDTO.getEmail(), otp);

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

    public void updatePassword(ReqRestPassword request) throws IdInValidException{
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new IdInValidException("Mật khẩu và mật khẩu xác nhận không trùng khớp. Vui lòng nhập lại");
        }

        User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IdInValidException("Không tồn tại tài khoản này trong hệ thống"));
        if(!user.isVerified()) {
            throw new IdInValidException("Tài khoản này chưa được xác thực");
        }

        if(user.getOtp()!=null) {
            throw new IdInValidException("Vui lòng xác thực OTP");
        }

        if(user.getExpirationTime()==null|| Instant.now().isAfter(user.getExpirationTime()) ){
            throw new IdInValidException("Bạn chưa xác thực OTP");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRefreshToken(null);
        // user.setExpirationTime(null);
        // user.setToken(null);
        this.userRepository.save(user);

    }

   

    

}
