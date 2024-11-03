package com.bytecinema.MovieTicketBookingSystem.controller;

import com.bytecinema.MovieTicketBookingSystem.domain.Booking;
import com.bytecinema.MovieTicketBookingSystem.dto.request.booking.ReqBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.booking.ResBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.info.ResponseInfo;
import com.bytecinema.MovieTicketBookingSystem.dto.response.vnpay.ResVnPayDTO;
import com.bytecinema.MovieTicketBookingSystem.service.BookingService;
import com.bytecinema.MovieTicketBookingSystem.service.SeatService;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    private final SeatService seatService;

    @PostMapping("/bookings")
    public ResponseEntity<ResBooking> createBooking(@RequestBody ReqBooking reqBooking) throws IdInValidException {
        Booking booking = bookingService.createBooking(reqBooking);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.bookingService.convertToResBooking(booking));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ResBooking> fetchBookingById(@RequestParam("bookingId") long bookingId) throws IdInValidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.bookingService.getBookingById(bookingId));
    }

    @GetMapping("/vn-pay")
    public ResponseEntity<ResVnPayDTO> payment(HttpServletRequest request) throws IdInValidException {
        return ResponseEntity.status(HttpStatus.OK).body(bookingService.createVnPayPayment(request));
    }

    @GetMapping("/vn-pay-callback")
    public ResponseEntity<ResponseInfo<String>> payCallbackHandler(HttpServletRequest request) throws IdInValidException {
        String status = request.getParameter("vnp_ResponseCode");
        String transactionCode = request.getParameter("vnp_TxnRef");
        log.info("Mã giao dịch: " + transactionCode);
        if (status.equals("00")) {
            this.bookingService.changeStatusBooking(request.getParameter("vnp_TxnRef"));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo<>("Payment successful"));
        } else {
            this.bookingService.handlePaymentFailure(transactionCode);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseInfo<>("Payment unsuccessful"));
        }
    }



}
