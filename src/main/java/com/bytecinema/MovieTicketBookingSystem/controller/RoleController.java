package com.bytecinema.MovieTicketBookingSystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytecinema.MovieTicketBookingSystem.domain.Role;
import com.bytecinema.MovieTicketBookingSystem.dto.ResRoleDTO;
import com.bytecinema.MovieTicketBookingSystem.service.RoleService;

@RestController
@RequestMapping("api/v1")
public class RoleController {
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    public ResponseEntity<ResRoleDTO> createRole(@RequestParam String roleName) {
        this.roleService.handleCreateRole(roleName);
        ResRoleDTO roleDTO = new ResRoleDTO();
        roleDTO.setName(roleName);
        return ResponseEntity.status(HttpStatus.OK).body(roleDTO);
    }

}
