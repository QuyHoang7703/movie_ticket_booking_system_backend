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

    public Role handleCreateRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return this.roleRepository.save(role);
    }

    
}
