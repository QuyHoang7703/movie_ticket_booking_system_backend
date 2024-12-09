package com.bytecinema.MovieTicketBookingSystem.dto.response.booking;

import com.bytecinema.MovieTicketBookingSystem.util.constant.StatusPayment;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResBooking {
    private String email;
    private String nameUser;

    private long bookingId;

    private String nameMovie;
    private String representativeMovieImage;
    private Duration duration;
    private String nation;
    private String language;

    private String formattedPerTicketPrice;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm dd-MM-yyy", timezone = "GMT+0")
    private Instant startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm dd-MM-yyy", timezone = "GMT+7")
    private Instant bookingTime;

    private String nameAuditorium;
    private int seatsNumber;
    private List<String> nameSeats;

//    private BigDecimal totalPrice;
    private String formattedTotalPrice;

    private StatusPayment statusPayment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm dd-MM-yyy", timezone = "GMT+7")
    private Instant paidTime;

    private String transactionCode;

}
