package com.bytecinema.MovieTicketBookingSystem.service;

import com.bytecinema.MovieTicketBookingSystem.config.VnPayConfig;
import com.bytecinema.MovieTicketBookingSystem.domain.*;
import com.bytecinema.MovieTicketBookingSystem.dto.request.booking.ReqBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.booking.ResBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.booking.ResGeneralCompletedBookingDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.pagination.ResultPaginationDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.vnpay.ResVnPayDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.BookingRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.ScreeningsRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.SeatsRepository;
import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import com.bytecinema.MovieTicketBookingSystem.util.VnPayUtil;
import com.bytecinema.MovieTicketBookingSystem.util.constant.StatusPayment;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
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
    private final EmailService emailService;
    private final RedisTemplate<String, ResultPaginationDTO> redisTemplate;
    private final ObjectMapper objectMapper;

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
        List<Long> idOfSeatsInAuditorium = screening.getAuditorium().getSeats().stream()
                .map(seat -> seat.getId()).toList();

        // Check id of seats belong id of Seats in auditorium
        boolean check = idOfSeatsInAuditorium.containsAll(idOfSeats);
        if(!check) {
            throw new IdInValidException("Seats in Auditorium not found");
        }

        List<Seat> seats = this.seatsRepository.findByIdIn(idOfSeats);
        List<Seat> orderedSeats = this.seatService.getOrderedSeats(reqBooking.getScreeningId());
        if(orderedSeats!=null && !orderedSeats.isEmpty()){
            for(Seat seat : seats){
                for(Seat orderedSeat : orderedSeats){
                    if(seat.getId() == orderedSeat.getId()){
                        throw new RuntimeException("Seat " + seat.getSeatRow() + seat.getSeatNumber() + " already ordered");
                    }
                }
            }
        }
        booking.setSeats(seats);

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
        resBooking.setStatusPayment(booking.getStatusPayment());
        resBooking.setPaidTime(booking.getPaymentTime());

        Movie movie = booking.getScreening().getMovie();
        resBooking.setRepresentativeMovieImage(movie.getImages().get(0).getImagePath());
        resBooking.setDuration(movie.getDuration());
        resBooking.setNation(movie.getNation());
        resBooking.setLanguage(movie.getLanguage());


        if(booking.getTransactionCode() != null){
            resBooking.setTransactionCode(booking.getTransactionCode());
        }
        List<Seat> seats = booking.getSeats();
        List<String> nameSeats = seats.stream()
                .map(seat -> seat.getSeatRow() + seat.getSeatNumber())
                .toList();
        resBooking.setSeatsNumber(seats.size());
        resBooking.setNameSeats(nameSeats);

        BigDecimal perTicketPrice = booking.getTicketPrice().divide(BigDecimal.valueOf(seats.size()), RoundingMode.HALF_UP);
        resBooking.setFormattedPerTicketPrice(this.formatCurrency(perTicketPrice));

        resBooking.setFormattedTotalPrice(this.formatCurrency(booking.getTicketPrice()));

        return resBooking;
    }

//    private String formatCurrency(BigDecimal amount) {
//        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//        return formatter.format(amount);
//    }
    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        formatter.setMinimumFractionDigits(0); // Loại bỏ phần thập phân nếu không cần thiết
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount) + " VND";
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
        if(Instant.now().isAfter(booking.getPaymentExpiryTime())){
            this.bookingRepository.delete(booking);
            throw new RuntimeException("Payment time is over !!!");
        }
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
        log.info("RESULT: " +VnPayUtil.getIpAddress(request));
        ResVnPayDTO res = new ResVnPayDTO();
        res.setMessage("Success");
        res.setPaymentUrl(paymentUrl);

        String transactionCode = vnpParamsMap.get("vnp_TxnRef");
        booking.setTransactionCode(transactionCode);
        this.bookingRepository.save(booking);
        return res;

    }

    public void sendOrderThroughEmail(String transactionCode) throws IdInValidException, IOException {
        Booking bookingDb = this.bookingRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new IdInValidException("Booking not found"));
        bookingDb.setStatusPayment(StatusPayment.PAID);
        bookingDb.setPaymentExpiryTime(null);
        Booking updatedBooking = this.bookingRepository.save(bookingDb);
        ResBooking resBooking = this.convertToResBooking(updatedBooking);
        String nameSeats = String.join(", ", resBooking.getNameSeats());
        Context context = new Context();
        context.setVariable("cssContent", this.emailService.loadCssFromFile());
        context.setVariable("resBooking", resBooking);
        context.setVariable("nameSeats", nameSeats);

        this.emailService.sendEmail(updatedBooking.getUser().getEmail(), "Order ticket cinema", "order2", context);
        log.info("Sent order through email");
    }

    public void handlePaymentFailure(String transactionCode) throws IdInValidException {
        Booking booking = this.bookingRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new IdInValidException("Booking not found"));
        this.bookingRepository.delete(booking);
        log.info("Deleted booking with id : " + booking.getId());
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void deleteExpiredBooking() throws IdInValidException {
        List<Booking> bookingList = this.bookingRepository.findByStatusPayment(StatusPayment.PENDING_PAYMENT);
        for(Booking booking : bookingList){
            if(Instant.now().isAfter(booking.getPaymentExpiryTime())){
                log.info("Deleted expired booking: " + booking.getId());
                this.bookingRepository.delete(booking);
            }
        }
    }

    public ResBooking getSuccessfulBooking(String transactionCode) throws IdInValidException {
        Booking bookingDb = this.bookingRepository.findByTransactionCode(transactionCode)
                .orElseThrow(()-> new IdInValidException("Can not find booking with transaction code : " + transactionCode));
        return this.convertToResBooking(bookingDb);
    }

    public ResGeneralCompletedBookingDTO convertToResGeneralCompletedBookingDTO(Booking booking) throws IdInValidException {
        ResGeneralCompletedBookingDTO res = new ResGeneralCompletedBookingDTO();
        Movie movie = booking.getScreening().getMovie();
        res.setBookingId(booking.getId());
        res.setNameMovie(movie.getName());
        res.setRepresentativeMovieImage(movie.getImages().get(0).getImagePath());
        res.setSeatsNumber(booking.getSeats().size());
        res.setFormattedTotalPrice(this.formatCurrency(booking.getTicketPrice()));
        res.setNameAuditorium(booking.getScreening().getAuditorium().getName());
        res.setStartTime(booking.getScreening().getStartTime());
        res.setPaidTime(booking.getScreening().getEndTime());

        return res;
    }

    public ResultPaginationDTO getAllGeneralCompletedBookings(Specification<Booking> spec, Pageable pageable, boolean isAlreadyScreened) {
        String email = SecurityUtil.getCurrentLogin().isPresent() ? SecurityUtil.getCurrentLogin().get() : null;

//        Object rawJson = redisTemplate.opsForValue().get("paidBooking_" + email + "_" + isAlreadyScreened + "_" + pageable.getPageNumber()+1 + "_" + pageable.getPageSize() + ":");
//        if(rawJson != null) {
//            ResultPaginationDTO res = objectMapper.convertValue(rawJson, ResultPaginationDTO.class);
//            log.info("Get order from redis");
//            return res;
//        }

        Specification<Booking> newSpec = (root, query, criteriaBuilder) -> {
            Join<Booking, User> joinUser = root.join("user");

            Predicate emailPredicate = criteriaBuilder.equal(joinUser.get("email"), email);

            Join<Booking, Screening> joinScreening = root.join("screening");

            Predicate statusCompletedBooking;
            if(!isAlreadyScreened){
                statusCompletedBooking = criteriaBuilder.greaterThan(joinScreening.get("startTime"), Instant.now());
            }else{
                statusCompletedBooking = criteriaBuilder.lessThan(joinScreening.get("startTime"), Instant.now());
            }

            Predicate isPaidPredicate = criteriaBuilder.equal(root.get("statusPayment"), StatusPayment.PAID);



            return criteriaBuilder.and(emailPredicate, statusCompletedBooking, isPaidPredicate);
        };

        Specification<Booking> finalSpec = spec.and(newSpec);
        Page<Booking> bookingPage = this.bookingRepository.findAll(finalSpec, pageable);

        ResultPaginationDTO res = new ResultPaginationDTO();

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber()+1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(bookingPage.getTotalPages());
        meta.setTotal(bookingPage.getTotalElements());

        res.setMeta(meta);

        List<ResGeneralCompletedBookingDTO> resList = bookingPage.getContent().stream()
                .map(item -> {
                    try {
                        return this.convertToResGeneralCompletedBookingDTO(item);
                    } catch (IdInValidException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        res.setResult(resList);

//        // Lưu paid booking vào redis
//        String key = "paidBooking_" + email + "_" + isAlreadyScreened + "_" + pageable.getPageNumber()+1 + "_" + pageable.getPageSize() + ":";
//        redisTemplate.opsForValue().set(key, res);

        return res;
    }



}
