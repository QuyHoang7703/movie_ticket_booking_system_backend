package com.bytecinema.MovieTicketBookingSystem.controller;

import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.pagination.ResultPaginationDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.register.ResUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.service.S3Service;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import com.turkraft.springfilter.boot.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.dto.response.login.ResLoginDTO;
import com.bytecinema.MovieTicketBookingSystem.service.UserService;
import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import com.bytecinema.MovieTicketBookingSystem.util.annatiation.ApiMessage;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class UserController {
    private final UserService userService;
//    private PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @GetMapping("/users/{id}")
    @ApiMessage("Fetch user by ID")
    public ResponseEntity<ResUserInfoDTO> fetchUserByID(@PathVariable("id") long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchUserById(id));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) {
        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    
    @GetMapping("/user-info")
    @ApiMessage("fetch user")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentLogin().isPresent()?
                            SecurityUtil.getCurrentLogin().get():"";
        User currentUser = this.userService.handleGetUserByEmail(email);
        ResLoginDTO.UserLogin userLogin = ResLoginDTO.UserLogin.builder()
                                    .id(currentUser.getId())
                                    .email(currentUser.getEmail())
                                    .name(currentUser.getName())
                                    .phoneNumber(currentUser.getPhoneNumber())
                                    // .birthDay(currentUser.getBirthDay())
                                    .gender(currentUser.getGender())
                                    .avatar(currentUser.getAvatar())
                                    .role(currentUser.getRole().getName())
                                    .build();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        userGetAccount.setUser(userLogin);
       
        return ResponseEntity.status(HttpStatus.OK).body(userGetAccount);
    }



    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Fetch all user")
    public ResponseEntity<ResultPaginationDTO> fetchAllUser(@Filter Specification<User> specification,
                                                            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchAllUsers(specification, pageable));

    }

    @PutMapping(value="/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Updated information for user")
    public ResponseEntity<ResUserInfoDTO> updateInfoUser(@RequestParam(value="fileAvatar", required = false) MultipartFile file, @RequestPart("user_info") ReqUserInfoDTO userInfoDTO) throws IdInValidException {
        ResUserInfoDTO resUserInfoDTO  = this.userService.updateUserInfo(file, userInfoDTO);
        return ResponseEntity.ok(resUserInfoDTO);
    }









    
}
