package com.bytecinema.MovieTicketBookingSystem.service;

import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.domain.Role;
import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.domain.dto.RegisterDTO;
import com.bytecinema.MovieTicketBookingSystem.domain.dto.ResUserDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.RoleRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.UserRepository;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;

import java.util.Optional;
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User handleCreateUser(RegisterDTO registerDTO) throws IdInValidException{
        Optional<Role> optionalRole = this.roleRepository.findById(registerDTO.getRoleId());
        if(!optionalRole.isPresent()) {
            throw new IdInValidException("Role is invalid");
        }
        Role role = optionalRole.get();
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        user.setRole(role);
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
 
    // public User fetchUserByEmail(String email) {
    //     return this.userRepository.findByEmail(email);
    // }

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
    
    public ResUserDTO convertToResUserRegister(User user){
        ResUserDTO resUser = new ResUserDTO();
        resUser.setId(user.getId());
        resUser.setEmail(user.getEmail());
        resUser.setName(user.getName());
        resUser.setPhoneNumber(user.getPhoneNumber());
        resUser.setMale(user.isMale());
        resUser.setAvatar(user.getAvatar());
    
        return resUser;
    }
}
