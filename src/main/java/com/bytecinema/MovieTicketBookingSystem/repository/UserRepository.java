package com.bytecinema.MovieTicketBookingSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bytecinema.MovieTicketBookingSystem.domain.User;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshTokenAndEmail(String refreshToken, String email);
    boolean existsByEmail(String email);
}
