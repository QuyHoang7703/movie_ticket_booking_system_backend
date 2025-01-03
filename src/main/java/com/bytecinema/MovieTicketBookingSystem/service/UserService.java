package com.bytecinema.MovieTicketBookingSystem.service;

import com.amazonaws.services.codecommit.model.UserInfo;
import com.bytecinema.MovieTicketBookingSystem.dto.request.account.ReqChangePasswordDTO;


import com.bytecinema.MovieTicketBookingSystem.dto.request.account.ReqUpdatePasswordDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.pagination.ResultPaginationDTO;
import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Security;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final S3Service s3Service;
   
    public User handleRegisterUser(ReqRegisterDTO registerDTO) throws IdInValidException, IOException {
        Optional<Role> optionalRole = this.roleRepository.findById(registerDTO.getRoleId());
        if(!optionalRole.isPresent()) {
            throw new IdInValidException("Role is invalid");
        }
        Role role = optionalRole.get();
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        user.setRole(role);
        user.setExpirationTime(Instant.now().plus(3, ChronoUnit.MINUTES));
        String otp = this.otpService.generateOTP();
        
        String otpDecoded = this.passwordEncoder.encode(otp);
        user.setOtp(otpDecoded);
        //Send OTP to email register
        this.otpService.sendVerificationEmail(registerDTO.getEmail(), otp);

        // Add attribute of user
        return this.userRepository.save(user);

    }

    public ResUserInfoDTO fetchUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if(userOptional.isPresent()) {
            User user = userOptional.get();
            ResUserInfoDTO resUserInfoDTO = new ResUserInfoDTO();

            ResUserInfoDTO.UserInfo userInfo = new ResUserInfoDTO.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setEmail(user.getEmail());
            userInfo.setName(user.getName());
            userInfo.setPhoneNumber(user.getPhoneNumber());
            userInfo.setBirthDay(user.getBirthDay());
            userInfo.setGender(user.getGender());
            userInfo.setAvatar(user.getAvatar());
            userInfo.setRole(user.getRole().getName());

            resUserInfoDTO.setUserInfo(userInfo);
            return resUserInfoDTO;

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
        ResUserInfoDTO resUserInfoDTO = new ResUserInfoDTO();
        ResUserInfoDTO.UserInfo userInfo = new ResUserInfoDTO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setName(user.getName());
        userInfo.setPhoneNumber(user.getPhoneNumber());
        userInfo.setBirthDay(user.getBirthDay());
        userInfo.setGender(user.getGender());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole().getName());

        resUserInfoDTO.setUserInfo(userInfo);
        return resUserInfoDTO;
    }

    public User handleUpdateUser(User reqUser) {
        long id = reqUser.getId();
        Optional<User> optionalUser = this.userRepository.findById(id);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(reqUser.getName());
            user.setPhoneNumber(reqUser.getPhoneNumber());
            user.setGender(reqUser.getGender());
            user.setAvatar(reqUser.getAvatar());
            user.setBirthDay(reqUser.getBirthDay());
            // userUpdate.setLockReason(reqUser.getLockReason());
            return this.userRepository.save(user);
        }
        return null;

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

    public ResultPaginationDTO fetchAllUsers(Specification<User> specification, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(specification, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber()+1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());
        resultPaginationDTO.setMeta(meta);

        List<ResUserInfoDTO> resUserInfoDTOList = pageUser.getContent().stream().map(user -> convertToResUserInfoDTO(user))
                        .collect(Collectors.toList());

        resultPaginationDTO.setResult(resUserInfoDTOList);
        return resultPaginationDTO;

    }

    public ResUserInfoDTO updateUserInfo(MultipartFile avatar, ReqUserInfoDTO requestUserInfoDTO) throws IdInValidException {
        String email = SecurityUtil.getCurrentLogin().isPresent() ? SecurityUtil.getCurrentLogin().get() : null;
        User currentUser = this.handleGetUserByEmail(email);
        if(currentUser == null) {
            throw new IdInValidException("User not found");
        }

//        user.setEmail(requestUserInfoDTO.getEmail());
        currentUser.setName(requestUserInfoDTO.getName());
        currentUser.setBirthDay(requestUserInfoDTO.getBirthDay());
        currentUser.setGender(requestUserInfoDTO.getGender());
        currentUser.setPhoneNumber(requestUserInfoDTO.getPhoneNumber());
        if(currentUser.getAvatar() != null) {
            this.s3Service.deleteFile(currentUser.getAvatar());
        }
        if(avatar!=null) {
            String urlAvatar = this.s3Service.uploadFile(avatar);
            currentUser.setAvatar(urlAvatar);
        }
        this.userRepository.save(currentUser);

        return this.convertToResUserInfoDTO(currentUser);

    }

    public void updatePassword(ReqUpdatePasswordDTO reqUpdatePasswordDTO) {
        String email = SecurityUtil.getCurrentLogin().isPresent() ? SecurityUtil.getCurrentLogin().get() : null;
        if(email==null) {
            throw new RuntimeException("Access token not available");
        }
        Optional<User> optionalUser = this.userRepository.findByEmail(email);
        if(!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();
        if(!passwordEncoder.matches(reqUpdatePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password does not match");
        }
        if(reqUpdatePasswordDTO.getCurrentPassword().equals(reqUpdatePasswordDTO.getNewPassword())){
            throw new RuntimeException("New password does not match current password");
        }
        if(!reqUpdatePasswordDTO.getNewPassword().equals(reqUpdatePasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("Confirm password does not match new password");
        }
        user.setPassword(this.passwordEncoder.encode(reqUpdatePasswordDTO.getNewPassword()));
//        user.setPasswordUpdatedAt(Instant.now());
        this.userRepository.save(user);

    }

}
