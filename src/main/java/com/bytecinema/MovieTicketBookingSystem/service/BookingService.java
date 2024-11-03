package com.bytecinema.MovieTicketBookingSystem.service;

import com.bytecinema.MovieTicketBookingSystem.config.VnPayConfig;
import com.bytecinema.MovieTicketBookingSystem.domain.Booking;
import com.bytecinema.MovieTicketBookingSystem.domain.Screening;
import com.bytecinema.MovieTicketBookingSystem.domain.Seat;
import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.dto.request.booking.ReqBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.booking.ResBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.vnpay.ResVnPayDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.BookingRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.ScreeningsRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.SeatsRepository;
import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import com.bytecinema.MovieTicketBookingSystem.util.VnPayUtil;
import com.bytecinema.MovieTicketBookingSystem.util.constant.StatusPayment;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ScreeningsRepository screeningsRepository;
    private final SeatsRepository seatsRepository;
    private final VnPayConfig vnPayConfig;
    private final SeatService seatService;

    public Booking createBooking(ReqBooking reqBooking) throws IdInValidException {
        Booking booking = new Booking();

        String email = SecurityUtil.getCurrentLogin().isPresent() ? SecurityUtil.getCurrentLogin().get() : "";
        User currentUser = this.userService.handleGetUserByEmail(email);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }
        booking.setUser(currentUser);

        Screening screening = this.screeningsRepository.findById(reqBooking.getScreeningId())
                .orElseThrow(() -> new IdInValidException("Screening not found"));
        booking.setScreening(screening);

        List<Long> idOfSeats = reqBooking.getSeats().stream().map(seat -> seat.getId())
                .toList();
        List<Seat> seats = this.seatsRepository.findByIdIn(idOfSeats);
        booking.setSeats(seats);
        for (Seat seat : seats) {
            this.seatService.updateStatusSeat(seat.getId(), false);
        }

        BigDecimal pricePerSeat = booking.getScreening().getTicketPrice();
        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(seats.size()));
        booking.setTicketPrice(totalPrice);
        booking.setStatusPayment(StatusPayment.PENDING_PAYMENT);

        return this.bookingRepository.save(booking);
    }

    public ResBooking convertToResBooking(Booking booking) {
        ResBooking resBooking = new ResBooking();

        resBooking.setEmail(booking.getUser().getEmail());
        resBooking.setNameUser(booking.getUser().getName());
        resBooking.setBookingId(booking.getId());
        resBooking.setNameMovie(booking.getScreening().getMovie().getName());
        resBooking.setStartTime(booking.getScreening().getStartTime());
        resBooking.setBookingTime(booking.getTimeBooking());
        resBooking.setNameAuditorium(booking.getScreening().getAuditorium().getName());
        List<Seat> seats = booking.getSeats();
        List<String> nameSeats = seats.stream()
                .map(seat -> seat.getSeatRow() + seat.getSeatNumber())
                .toList();
        resBooking.setNameSeats(nameSeats);
//        BigDecimal pricePerSeat = booking.getScreening().getTicketPrice();
//        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(seats.size()));
        resBooking.setFormattedTotalPrice(this.formatCurrency(booking.getTicketPrice()));
        return resBooking;
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    public ResBooking getBookingById(long bookingId) throws IdInValidException {
        String email = SecurityUtil.getCurrentLogin().isPresent() ? SecurityUtil.getCurrentLogin().get() : "";
        User currentUser = this.userService.handleGetUserByEmail(email);
        if (currentUser == null) {
            throw new IdInValidException("User not found");
        }

        Booking booking = this.bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IdInValidException("Booking not found"));
        if(!booking.getUser().equals(currentUser)) {
            throw new RuntimeException("User not authorized");
        }


        return this.convertToResBooking(booking);
    }

    public ResVnPayDTO createVnPayPayment(HttpServletRequest request) throws IdInValidException {
        Booking booking = this.bookingRepository.findById(Long.valueOf((request.getParameter("bookingId"))))
                .orElseThrow(() -> new IdInValidException("Booking not found"));

//        long amount = Integer.parseInt(String.valueOf(10000)) * 100L;
        BigDecimal ticketPrice = booking.getTicketPrice();
        BigDecimal roundedPrice = ticketPrice.setScale(0, RoundingMode.DOWN);
        long amount = roundedPrice.multiply(BigDecimal.valueOf(100)).longValue();
        log.info("AMOUNT: " + amount);

        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = vnPayConfig.getVnPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VnPayUtil.getIpAddress(request));
        //build query url
        String queryUrl = VnPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VnPayUtil.getPaymentURL(vnpParamsMap, false);
        queryUrl += "&vnp_SecureHash=" + VnPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        ResVnPayDTO res = new ResVnPayDTO();
        res.setMessage("Success");
        res.setPaymentUrl(paymentUrl);

        String transactionCode = vnpParamsMap.get("vnp_TxnRef");
        booking.setTransactionCode(transactionCode);
        this.bookingRepository.save(booking);
        return res;

    }

    public void changeStatusBooking(String transactionCode) throws IdInValidException {
        Booking bookingDb = this.bookingRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new IdInValidException("Booking not found"));
        bookingDb.setStatusPayment(StatusPayment.PAID);
        this.bookingRepository.save(bookingDb);
    }

    public void handlePaymentFailure(String transactionCode) throws IdInValidException {
        Booking booking = this.bookingRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new IdInValidException("Booking not found"));
        List<Seat> seats = booking.getSeats();
        for (Seat seat : seats) {
            this.seatService.updateStatusSeat(seat.getId(), true);
        }
    }


}
