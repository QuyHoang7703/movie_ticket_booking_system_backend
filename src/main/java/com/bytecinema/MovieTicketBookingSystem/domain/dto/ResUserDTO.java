package com.bytecinema.MovieTicketBookingSystem.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUserDTO {
    private long id;

    private String email;

    private String name;

    private String phoneNumber;

    private boolean male;

    private String avatar;

  
}
