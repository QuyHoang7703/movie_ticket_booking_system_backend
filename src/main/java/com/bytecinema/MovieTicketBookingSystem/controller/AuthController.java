package com.bytecinema.MovieTicketBookingSystem.controller;

import com.bytecinema.MovieTicketBookingSystem.dto.request.account.ReqChangePasswordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.dto.request.login.ReqLoginDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqRegisterDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqVerifyDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.info.ResponseInfo;
import com.bytecinema.MovieTicketBookingSystem.dto.response.login.ResLoginDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.register.ResUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.service.OtpService;
import com.bytecinema.MovieTicketBookingSystem.service.S3Service;
import com.bytecinema.MovieTicketBookingSystem.service.TokenService;
import com.bytecinema.MovieTicketBookingSystem.service.UserService;
import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import com.bytecinema.MovieTicketBookingSystem.util.annatiation.ApiMessage;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final OtpService otpService;
    private final TokenService tokenService;
    @Value("${bytecinema.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @Value("${bytecinema.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @PostMapping("auth/register")
    @ApiMessage("Register a new user")
    public ResponseEntity<ResponseInfo<String>> createUser(@RequestBody ReqRegisterDTO registerDTO) throws IdInValidException{
        if(this.userService.checkAvailableEmail(registerDTO.getEmail())){
            User user = this.userService.handleGetUserByEmail(registerDTO.getEmail());
            if(!user.isVerified()) {
                throw new IdInValidException("Email này đã được đăng ký nhưng chưa xác nhận. Vui lòng xác nhận");
            }
            throw new IdInValidException("Email này đã được đăng ký rồi");
        }

        if(!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())){
            throw new IdInValidException("Mật khẩu và xác nhận mật khẩu không trùng khớp");
        }
        String hashPassword = this.passwordEncoder.encode(registerDTO.getPassword());
        registerDTO.setPassword(hashPassword);
        User newUser = this.userService.handleRegisterUser(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseInfo<>("Kiểm tra email để lấy OTP"));
    }

    @PostMapping(value="auth/register-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Register a new user")
    public ResponseEntity<ResUserInfoDTO> addInfoUser(@RequestParam(value="fileAvatar", required = false) MultipartFile file, @RequestPart("user_info") ReqUserInfoDTO userInfoDTO) throws IdInValidException{
        User user = this.userService.handleGetUserByEmail(userInfoDTO.getEmail());
        if(user==null) {
            throw new IdInValidException("Không tồn tại email này trong hệ thống");
        }
        user.setName(userInfoDTO.getName());
        user.setBirthDay(userInfoDTO.getBirthDay());
        user.setGender(userInfoDTO.getGender());
        user.setPhoneNumber(userInfoDTO.getPhoneNumber());

        if(file!=null) {
            String avatar = this.s3Service.uploadFile(file);
            user.setAvatar(avatar);
        }

        return ResponseEntity.ok(this.userService.convertToResUserInfoDTO(this.userService.handleUpdateUser(user)));
    }

    @PostMapping("/auth/verify-otp")
    public ResponseEntity<ResponseInfo<String>> verify(@RequestBody ReqVerifyDTO verifyDTO) throws IdInValidException{
        if(!this.userService.checkAvailableEmail(verifyDTO.getEmail())){
            throw new IdInValidException("Email not found");
        }
        this.otpService.verify(verifyDTO.getEmail(), verifyDTO.getOtp());

        return ResponseEntity.ok(new ResponseInfo<>("Verified successful"));
    }

    @PostMapping("/auth/resend")
    public ResponseEntity<ResponseInfo<String>> resendOTP(@RequestParam String email) throws IdInValidException{
        this.otpService.resendOtp(email);
        return ResponseEntity.ok(new ResponseInfo<>("Resend OTP"));

    }


    @PostMapping("/auth/login")
    @ApiMessage("Login successfully")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO ReqLoginDTO) throws IdInValidException{
        if(!this.userService.handleGetUserByEmail(ReqLoginDTO.getEmail()).isVerified()){
            throw new IdInValidException("User not found !!!");
        }
        //Load username and password into Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(ReqLoginDTO.getEmail(), ReqLoginDTO.getPassword());
        //User Authentication => overwrite LoadUserByUsername in UserDetailService
        Authentication authentication = this.authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = this.userService.handleGetUserByEmail(ReqLoginDTO.getEmail());
        log.info("User role: " + user.getRole().getName());
        ResLoginDTO res = new ResLoginDTO();
        if(user != null){
            ResLoginDTO.UserLogin userLogin = ResLoginDTO.UserLogin.builder()
                                                .id(user.getId())
                                                .email(user.getEmail())
                                                .name(user.getName())
                                                .phoneNumber(user.getPhoneNumber())
                                                .gender(user.getGender())
                                                .avatar(user.getAvatar())
                                                .role(user.getRole().getName())
                                                .build();
            res.setUserLogin(userLogin);
        }

        // // Create token when authentication is successful
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(accessToken);
        ResponseCookie accCookies = this.securityUtil.createAccessCookie("access_token", accessToken, accessTokenExpiration);
        

        // Create refresh token 
        String refresh_token = this.securityUtil.createRefreshToken(ReqLoginDTO.getEmail(), res);
        this.userService.updateRefreshToken(refresh_token, ReqLoginDTO.getEmail());
        ResponseCookie resCookies = this.securityUtil.createRefreshCookie("refresh_token", refresh_token, refreshTokenExpiration);
        
        
        return ResponseEntity
                            .status(HttpStatus.OK)
                            .header(HttpHeaders.SET_COOKIE, accCookies.toString())
                            .header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(res);
    }


    @GetMapping("/auth/refresh")
    @ApiMessage("Refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(@CookieValue(name ="refresh_token", defaultValue = "noRefreshTokenInCookie") String refreshToken) throws IdInValidException {
        if(refreshToken.equals("noRefreshTokenInCookie")){
            throw new IdInValidException("You don't have refresh token in Cookie");
        }
        //Check valid refresh token
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();
        User user = this.userService.fetchUserByRefreshTokenAndEmail(refreshToken, email);
       
        ResLoginDTO res = new ResLoginDTO();
        if(user==null) {
            throw new IdInValidException("Refresh Token is invalid");
        }else{
  
            // ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getName(), user.getAvatar());
            ResLoginDTO.UserLogin userLogin = ResLoginDTO.UserLogin.builder()
                                                .id(user.getId())
                                                .email(user.getEmail())
                                                .name(user.getName())
                                                .phoneNumber(user.getPhoneNumber())
                                                // .birthDay(user.getBirthDay())
                                                .gender(user.getGender())
                                                .avatar(user.getAvatar())
                                                .role(user.getRole().getName())
                                                .build();
            res.setUserLogin(userLogin);
        }
  
        String accessToken = this.securityUtil.createAccessToken(email, res);
        res.setAccessToken(accessToken);

        ResponseCookie accCookies = this.securityUtil.createAccessCookie("access_token", accessToken, accessTokenExpiration);

        // Create refresh token 
        String refresh_token = this.securityUtil.createRefreshToken(email, res);
        this.userService.updateRefreshToken(refresh_token, email);
        ResponseCookie resCookies = this.securityUtil.createRefreshCookie("refresh_token", refresh_token, refreshTokenExpiration);

        return ResponseEntity
                            .status(HttpStatus.OK)
                            .header(HttpHeaders.SET_COOKIE, accCookies.toString())
                            .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                            .body(res);
     
    }

    @PostMapping("/auth/logout")
    @ApiMessage("Logout account")
    public ResponseEntity<Void> logout() throws IdInValidException{
        String username = SecurityUtil.getCurrentLogin().isPresent()?
                            SecurityUtil.getCurrentLogin().get() : "";
        if(username==null) {
            throw new IdInValidException("Access token is invalid");
        }
        this.userService.updateRefreshToken(null, username);
        ResponseCookie accCookies = this.securityUtil.createAccessCookie("access_token", null, 0);
        ResponseCookie resCookies = this.securityUtil.createRefreshCookie("refresh_token", null, 0);
        System.out.println(">>>>> Logout account username: " + username);
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accCookies.toString())
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(null);
    }

    @GetMapping("/auth/forgot-password")
    @ApiMessage("Send request restore password")
    public ResponseEntity<ResponseInfo<String>> sendRequestForgotPassword(@RequestParam("email") String email) throws IdInValidException {
        this.tokenService.createToken(email);
       
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo<>("Yêu cầu của bạn đã được gửi tới email"));
    }

    
    @GetMapping("/auth/verify-token")
    @ApiMessage("Send request restore password")
    public ResponseEntity<ResponseInfo<Boolean>> checkValidToken(@RequestParam("token") String token) {
        
       
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo<>(this.tokenService.isValidToken(token)));
    }

    @PostMapping("/auth/change-password")
    public ResponseEntity<ResponseInfo<String>> changePassword(@RequestBody ReqChangePasswordDTO changePasswordDTO) throws IdInValidException{
        this.userService.handleChangePassword(changePasswordDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo<>("Đã thay đổi mật khẩu"));
    }



}
