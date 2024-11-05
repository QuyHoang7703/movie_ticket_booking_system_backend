package com.bytecinema.MovieTicketBookingSystem.dto.response.vnpay;

import lombok.Builder;
import lombok.Data;

@Data
public class ResVnPayDTO {
    public String message;
    private String paymentUrl;
}
