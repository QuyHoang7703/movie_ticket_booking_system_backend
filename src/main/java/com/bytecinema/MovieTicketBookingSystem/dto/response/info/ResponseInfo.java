package com.bytecinema.MovieTicketBookingSystem.dto.response.info;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class ResponseInfo<T> {
    private T info;
}

