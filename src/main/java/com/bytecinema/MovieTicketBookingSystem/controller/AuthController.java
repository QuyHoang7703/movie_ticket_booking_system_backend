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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytecinema.MovieTicketBookingSystem.domain.RestResponse;
import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.domain.dto.LoginDTO;
import com.bytecinema.MovieTicketBookingSystem.domain.dto.ResLoginDTO;
import com.bytecinema.MovieTicketBookingSystem.service.UserService;
import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import com.bytecinema.MovieTicketBookingSystem.util.annatiation.ApiMessage;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;

import jakarta.validation.Valid;
@RestController
@RequestMapping("api/v1")
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    @Value("${bytecinema.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    
    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }


    @PostMapping("/auth/login")
    @ApiMessage("Login successfully")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        //Load username and password into Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
        //User Authentication => overwrite LoadUserByUsername in UserDetailService
        Authentication authentication = this.authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        User user = this.userService.handleGetUserByEmail(loginDTO.getEmail());
        ResLoginDTO res = new ResLoginDTO();
        if(user != null){
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getName());
            res.setUserLogin(userLogin);
        }
    
        // Create token when authentication is successful
        // String u = authentication.getName();
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(accessToken);
        

        // Create refresh token 
        String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getEmail(), res);
        this.userService.updateRefreshToken(refresh_token, loginDTO.getEmail());
        ResponseCookie resCookies = ResponseCookie
                                                .from("refresh_token", refresh_token)
                                                .httpOnly(true)
                                                .secure(true)
                                                .path("/")
                                                .maxAge(refreshTokenExpiration)
                                                // .domain("example.com")
                                                .build();
        
        
        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(res);
    }

    @GetMapping("/auth/users")
    @ApiMessage("fetch user")
    public ResponseEntity<ResLoginDTO.UserLogin> getAccount() {
        String email = SecurityUtil.getCurrentLogin().isPresent()?
                            SecurityUtil.getCurrentLogin().get():"";
        User currentUser = this.userService.handleGetUserByEmail(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        if(currentUser != null){
            userLogin.setId(currentUser.getId());
            userLogin.setUsername(currentUser.getEmail());
            userLogin.setName(currentUser.getName());
        }
        return ResponseEntity.status(HttpStatus.OK).body(userLogin);
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
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getName());
            res.setUserLogin(userLogin);
        }
  
        String accessToken = this.securityUtil.createAccessToken(email, res);
        res.setAccessToken(accessToken);
        

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
        
        
        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(res);
     
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
}
