package com.bytecinema.MovieTicketBookingSystem.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

import com.bytecinema.MovieTicketBookingSystem.domain.RestResponse;
import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.dto.request.login.ReqLoginDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqRegisterDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqVerifyDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.resetPassword.ReqRestPassword;
import com.bytecinema.MovieTicketBookingSystem.dto.response.info.ResponseInfo;
import com.bytecinema.MovieTicketBookingSystem.dto.response.login.ResLoginDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.login.ResLoginDTO.UserGetAccount;
import com.bytecinema.MovieTicketBookingSystem.dto.response.login.ResLoginDTO.UserLogin;
import com.bytecinema.MovieTicketBookingSystem.dto.response.register.ResUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.service.OtpService;
import com.bytecinema.MovieTicketBookingSystem.service.S3Service;
import com.bytecinema.MovieTicketBookingSystem.service.UserService;
import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import com.bytecinema.MovieTicketBookingSystem.util.annatiation.ApiMessage;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final OtpService otpService;
    @Value("${bytecinema.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @Value("${bytecinema.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @PostMapping("auth/register")
    @ApiMessage("Register a new user")
    public ResponseEntity<ResponseInfo> createUser(@RequestBody ReqRegisterDTO registerDTO) throws IdInValidException{
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
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseInfo("Kiểm tra email để lấy OTP"));
    }

    @PostMapping("auth/register-info")
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
            String avatar = this.s3Service.uploadFile(file, "avatars");
            user.setAvatar(avatar);
        }

        return ResponseEntity.ok(this.userService.convertToResUserInfoDTO(this.userService.handleUpdateUser(user)));
    }

    @PostMapping("/auth/verify-otp")
    public ResponseEntity<ResponseInfo> verify(@RequestBody ReqVerifyDTO verifyDTO) throws IdInValidException{
        if(!this.userService.checkAvailableEmail(verifyDTO.getEmail())){
            throw new IdInValidException("Email not found");
        }
        this.otpService.verify(verifyDTO.getEmail(), verifyDTO.getOtp());

        return ResponseEntity.ok(new ResponseInfo("Verified successful"));
    }

    @PostMapping("/auth/resend")
    public ResponseEntity<ResponseInfo> resendOTP(@RequestParam String email) throws IdInValidException{
        this.otpService.resendOtp(email);
        return ResponseEntity.ok(new ResponseInfo("Resend OTP"));

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
        ResLoginDTO res = new ResLoginDTO();
        if(user != null){
            // ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getName(), user.getPhoneNumber(), user.getBirthDay(), user.getGender(), user.getAvatar());
            // ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getName(), user.getAvatar());
            ResLoginDTO.UserLogin userLogin = ResLoginDTO.UserLogin.builder()
                                                .id(user.getId())
                                                .email(user.getEmail())
                                                .name(user.getName())
                                                .phoneNumber(user.getPhoneNumber())
                                                .gender(user.getGender())
                                                .avatar(user.getAvatar())  
                                                .build();


            res.setUserLogin(userLogin);
        }

        // // Create token when authentication is successful
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(accessToken);
        ResponseCookie accCookies = ResponseCookie
                                                .from("access_token", accessToken)
                                                // .httpOnly(true)
                                                .secure(true)
                                                .path("/")
                                                .maxAge(accessTokenExpiration)
                                                .build();
        

        // Create refresh token 
        String refresh_token = this.securityUtil.createRefreshToken(ReqLoginDTO.getEmail(), res);
        this.userService.updateRefreshToken(refresh_token, ReqLoginDTO.getEmail());
        ResponseCookie resCookies = ResponseCookie
                                                .from("refresh_token", refresh_token)
                                                .httpOnly(true)
                                                .secure(true)
                                                .path("/")
                                                .maxAge(refreshTokenExpiration)
                                                // .domain("example.com")
                                                .build();
        
        
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
                                                .build();

            res.setUserLogin(userLogin);
        }
  
        String accessToken = this.securityUtil.createAccessToken(email, res);
        res.setAccessToken(accessToken);

        ResponseCookie accCookies = ResponseCookie
                                                .from("access_token", accessToken)
                                                // .httpOnly(true)
                                                .secure(true)
                                                .path("/")
                                                .maxAge(accessTokenExpiration)
                                                .build();
        

        // Create refresh token 
        String refresh_token = this.securityUtil.createRefreshToken(email, res);
        this.userService.updateRefreshToken(refresh_token, email);
        ResponseCookie resCookies = ResponseCookie
                                                .from("refresh_token", refresh_token)
                                                .httpOnly(true)
                                                .secure(true)
                                                .path("/")
                                                .maxAge(refreshTokenExpiration)
                                                // .domain("example.com")
                                                .build();
        
        
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
        ResponseCookie resCookies = ResponseCookie
                                                .from("refresh_token", null)
                                                .httpOnly(true)
                                                .secure(true)
                                                .path("/")
                                                .maxAge(0)
                                                // .domain("example.com")
                                                .build();
        System.out.println(">>>>> Logout account username: " + username);
        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(null);
    }

    @PostMapping("/auth/reset-password-request")
    public ResponseEntity<ResponseInfo> forgotPassword(@RequestParam("email") String email) {
        try {
            this.otpService.sendRequestForgotPassword(email);
        } catch (IdInValidException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo(e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo("Vui lòng kiểm tra email để lấy OTP"));
    }

    @PostMapping("/auth/verify-otp-forgot-password")
    public ResponseEntity<ResponseInfo> checkToken(@RequestBody ReqVerifyDTO verifyDTO){
        try {
            this.otpService.verify_otp_forgot(verifyDTO.getEmail(), verifyDTO.getOtp());
        } catch (IdInValidException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo(e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo("Xác thực thành công"));
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<ResponseInfo> resetPassword(@RequestBody ReqRestPassword request) {
        try {
            this.userService.updatePassword(request);
        } catch (IdInValidException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo(e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo("Đã cập nhập lại mật khẩu. Vui lòng đăng nhập lại"));
    }


}
