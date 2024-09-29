package com.bytecinema.MovieTicketBookingSystem.service;

import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.domain.Role;
import com.bytecinema.MovieTicketBookingSystem.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role handleCreateRole(Role role) {
        return this.roleRepository.save(role);
    }

    
}
