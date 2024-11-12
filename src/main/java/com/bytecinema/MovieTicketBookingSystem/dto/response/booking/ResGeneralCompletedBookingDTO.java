package com.bytecinema.MovieTicketBookingSystem.dto.response.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
@Data
public class ResGeneralCompletedBookingDTO {
    private long bookingId;
    private String nameMovie;
    private String representativeMovieImage;
    private int seatsNumber;
    private String formattedTotalPrice;
    private String nameAuditorium;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm dd-MM-yyy", timezone = "GMT+7")
    private Instant startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm dd-MM-yyy", timezone = "GMT+7")
    private Instant paidTime;

}
