package com.bytecinema.MovieTicketBookingSystem.service;

import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.repository.UserRepository;
import java.util.Optional;
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleCreateUser(User user) {
        return this.userRepository.save(user);
    }

    public User fetchUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if(userOptional.isPresent()) {
            return userOptional.get();
        }
        return null;
    }
 
    public User fetchUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    
    
}
