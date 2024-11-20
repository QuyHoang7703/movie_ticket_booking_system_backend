package com.bytecinema.MovieTicketBookingSystem.config;

import com.bytecinema.MovieTicketBookingSystem.domain.Role;
import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.repository.RoleRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            // Check if the ADMIN role exists before saving
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                this.roleRepository.save(adminRole);
            }

            // Check if the USER role exists before saving
            if (roleRepository.findByName("USER").isEmpty()) {
                Role userRole = new Role();
                userRole.setName("USER");
                this.roleRepository.save(userRole);
            }

            if(userRepository.findAll().isEmpty()) {
                User user = new User();
                user.setEmail("admin@gmail.com");
                user.setPassword(passwordEncoder.encode("admin_cnpm"));
                user.setName("I AM ADMIN");
                Role role = this.roleRepository.findByName("ADMIN").get();
                user.setRole(role);
                user.setVerified(true);
                this.userRepository.save(user);
                log.info("Finish initial database");
            }

        };
    }
    
}
