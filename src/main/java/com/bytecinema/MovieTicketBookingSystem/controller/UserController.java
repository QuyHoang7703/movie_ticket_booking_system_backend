package com.bytecinema.MovieTicketBookingSystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.domain.dto.RegisterDTO;
import com.bytecinema.MovieTicketBookingSystem.domain.dto.ResUserDTO;
import com.bytecinema.MovieTicketBookingSystem.service.UserService;
import com.bytecinema.MovieTicketBookingSystem.util.annatiation.ApiMessage;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;

@RestController
@RequestMapping("api/v1")
public class UserController {
    private final UserService userService;
    private PasswordEncoder passwordEncoder;
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @ApiMessage("Register a new user")
    public ResponseEntity<ResUserDTO> createUser(@RequestBody RegisterDTO registerDTO) throws IdInValidException{
        if(this.userService.checkAvailableEmail(registerDTO.getEmail())){
            throw new IdInValidException("Email already exist, please use another one");
        }
        if(!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())){
            throw new IdInValidException("Password and Confirm Password do not match");
        }
        String hashPassword = this.passwordEncoder.encode(registerDTO.getPassword());
        registerDTO.setPassword(hashPassword);
        User newUser = this.userService.handleCreateUser(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResUserRegister(newUser));
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Fetch user by ID")
    public ResponseEntity<User> fecthUserByID(@PathVariable("id") long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchUserById(id));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) {
        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }



    
}
