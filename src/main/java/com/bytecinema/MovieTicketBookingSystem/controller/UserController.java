package com.bytecinema.MovieTicketBookingSystem.controller;

import com.bytecinema.MovieTicketBookingSystem.domain.Role;
import com.bytecinema.MovieTicketBookingSystem.dto.request.account.ReqUpdatePasswordDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.request.register.ReqUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.pagination.ResultPaginationDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.register.ResUserInfoDTO;
import com.bytecinema.MovieTicketBookingSystem.service.S3Service;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
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
    private final S3Service s3Service;
    private final SecurityUtil securityUtil;

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
                                                            @PageableDefault(size = 8) Pageable pageable) {
        Specification<User> roleNotAdmminSpec = (root, query, criteriaBuilder) -> {
            Join<User, Role> userRole = root.join("role");
            return criteriaBuilder.notEqual(userRole.get("name"), "ADMIN");
        };
        Specification<User> finalSpec = specification.and(roleNotAdmminSpec);

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchAllUsers(finalSpec, pageable));

    }

    @PutMapping(value="/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Updated information for user")
    public ResponseEntity<ResUserInfoDTO> updateInfoUser(@RequestParam(value="fileAvatar", required = false) MultipartFile file, @RequestPart("user_info") ReqUserInfoDTO userInfoDTO) throws IdInValidException {
        ResUserInfoDTO resUserInfoDTO  = this.userService.updateUserInfo(file, userInfoDTO);
        return ResponseEntity.ok(resUserInfoDTO);
    }

    @PatchMapping("/users/update-password")
    @ApiMessage("Updated password")
    public ResponseEntity<Void> updatePassword(@RequestBody ReqUpdatePasswordDTO reqUpdatePasswordDTO) {
        this.userService.updatePassword(reqUpdatePasswordDTO);
        // Xóa access token và refresh token hiện có
        ResponseCookie accCookies = this.securityUtil.createAccessCookie("access_token", null, 0);
        ResponseCookie resCookies = this.securityUtil.createRefreshCookie("refresh_token", null, 0);
        System.out.println(">>>>> Logout account username: ");
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accCookies.toString())
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(null);

    }









    
}
