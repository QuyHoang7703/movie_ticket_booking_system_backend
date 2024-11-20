package com.bytecinema.MovieTicketBookingSystem.controller;

import com.bytecinema.MovieTicketBookingSystem.domain.Booking;
import com.bytecinema.MovieTicketBookingSystem.dto.request.booking.ReqBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.booking.ResBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.info.ResponseInfo;
import com.bytecinema.MovieTicketBookingSystem.dto.response.pagination.ResultPaginationDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.vnpay.ResVnPayDTO;
import com.bytecinema.MovieTicketBookingSystem.service.BookingService;
import com.bytecinema.MovieTicketBookingSystem.service.SeatService;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
        ResBooking resBooking = this.bookingService.convertToResBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(resBooking);
    }

    @GetMapping("/bookings/detail")
    public ResponseEntity<ResBooking> fetchBookingById(@RequestParam("bookingId") long bookingId) throws IdInValidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.bookingService.getBookingById(bookingId));
    }

    @GetMapping("/vn-pay")
    public ResponseEntity<ResVnPayDTO> payment(HttpServletRequest request) throws IdInValidException {
        return ResponseEntity.status(HttpStatus.OK).body(bookingService.createVnPayPayment(request));
    }

    @GetMapping("/vn-pay-callback")
    @Transactional
    public ResponseEntity<ResponseInfo<String>> payCallbackHandler(HttpServletRequest request) throws IdInValidException, IOException {
        String status = request.getParameter("vnp_ResponseCode");
        String transactionCode = request.getParameter("vnp_TxnRef");
        log.info("Mã giao dịch: " + transactionCode);
        if (status.equals("00")) {
            this.bookingService.sendOrderThroughEmail(request.getParameter("vnp_TxnRef"));
//            return ResponseEntity.status(HttpStatus.OK).body(new ResponseInfo<>("Payment successful"));
            log.info("PAYMENT SUCCESSFULLY");
//            return ResponseEntity.status(HttpStatus.FOUND)
//                    .header(HttpHeaders.LOCATION, "http://localhost:3000/payment-success?transactionId=" + transactionCode)
//                    .build();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "https://byte-cinema-website.vercel.app/payment-success?transactionId=" + transactionCode)
                    .build();

        } else {
            this.bookingService.handlePaymentFailure(transactionCode);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseInfo<>("Payment unsuccessful"));
            log.info("PAYMENT UNSUCCESSFULLY");
//            return ResponseEntity.status(HttpStatus.FOUND)
//                    .header(HttpHeaders.LOCATION, "http://localhost:3000/payment-failure?transactionId=" + transactionCode)
//                    .build();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "https://byte-cinema-website.vercel.app/payment-failure?transactionId=" + transactionCode)
                    .build();
        }
    }

    @GetMapping("/bookings/successful")
    public ResponseEntity<ResBooking> paymentSuccess(@RequestParam("transactionCode") String transactionCode) throws IdInValidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.bookingService.getSuccessfulBooking(transactionCode));
    }

    @GetMapping("bookings/get-all")
    public ResponseEntity<ResultPaginationDTO> getAllGeneralCompletedBookings(@Filter Specification<Booking> spec,
                                                                              Pageable pageable,
                                                                              @RequestParam("isAlreadyScreened") boolean isAlreadyScreened){
        return ResponseEntity.status(HttpStatus.OK).body(this.bookingService.getAllGeneralCompletedBookings(spec, pageable, isAlreadyScreened));
    }

}
