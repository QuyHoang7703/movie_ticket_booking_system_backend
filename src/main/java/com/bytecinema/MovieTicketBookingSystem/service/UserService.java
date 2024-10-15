package com.bytecinema.MovieTicketBookingSystem.service;

import com.bytecinema.MovieTicketBookingSystem.dto.request.account.ReqChangePasswordDTO;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.domain.Role;
import com.bytecinema.MovieTicketBookingSystem.domain.User;

import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqRegisterDTO;
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
        Optional<User> optionaluser = this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
        if(optionaluser.isPresent()) {
            return optionaluser.get();
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

    public void handleChangePassword(ReqChangePasswordDTO changePasswordDTO) throws IdInValidException {
        Optional<User> optionalUser = this.userRepository.findByToken(changePasswordDTO.getToken());
        if(!optionalUser.isPresent()) {
            throw new IdInValidException("Token không hợp lệ");
        }
        if(!changePasswordDTO.getPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new IdInValidException("Mật khẩu không trùng khớp");
        }
        User user = optionalUser.get();

        String decodedPassword = this.passwordEncoder.encode(changePasswordDTO.getPassword());
        user.setPassword(decodedPassword);
        user.setExpirationTime(null);
        user.setToken(null);
        this.userRepository.save(user);

    }

   

    

}
