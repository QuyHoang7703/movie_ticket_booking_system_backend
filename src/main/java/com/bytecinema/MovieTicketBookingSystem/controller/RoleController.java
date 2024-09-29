package com.bytecinema.MovieTicketBookingSystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytecinema.MovieTicketBookingSystem.domain.Role;
import com.bytecinema.MovieTicketBookingSystem.domain.dto.RoleDTO;
import com.bytecinema.MovieTicketBookingSystem.service.RoleService;

@RestController
@RequestMapping("api/v1")
public class RoleController {
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    public ResponseEntity<RoleDTO> createRole(@RequestBody Role role) {
        this.roleService.handleCreateRole(role);
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName(role.getName());
        return ResponseEntity.status(HttpStatus.OK).body(roleDTO);
    }

}
