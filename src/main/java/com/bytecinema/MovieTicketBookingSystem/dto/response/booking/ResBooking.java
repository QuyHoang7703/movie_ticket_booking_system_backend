package com.bytecinema.MovieTicketBookingSystem.dto.response.booking;

import com.bytecinema.MovieTicketBookingSystem.util.constant.StatusPayment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
@Data
public class ResBooking {
    private String email;
    private String nameUser;

    private long bookingId;

    private String nameMovie;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm dd-MM-yyy", timezone = "GMT+7")
    private Instant startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm dd-MM-yyy", timezone = "GMT+7")
    private Instant bookingTime;

    private String nameAuditorium;
    private List<String> nameSeats;

//    private BigDecimal totalPrice;
    private String formattedTotalPrice;

    private StatusPayment statusPayment = StatusPayment.PENDING_PAYMENT;
}
