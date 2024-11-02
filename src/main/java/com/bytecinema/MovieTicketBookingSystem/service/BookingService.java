package com.bytecinema.MovieTicketBookingSystem.service;

import com.bytecinema.MovieTicketBookingSystem.domain.Booking;
import com.bytecinema.MovieTicketBookingSystem.domain.Screening;
import com.bytecinema.MovieTicketBookingSystem.domain.Seat;
import com.bytecinema.MovieTicketBookingSystem.domain.User;
import com.bytecinema.MovieTicketBookingSystem.dto.request.booking.ReqBooking;
import com.bytecinema.MovieTicketBookingSystem.dto.response.booking.ResBooking;
import com.bytecinema.MovieTicketBookingSystem.repository.BookingRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.ScreeningsRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.SeatsRepository;
import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import com.bytecinema.MovieTicketBookingSystem.util.error.IdInValidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ScreeningsRepository screeningsRepository;
    private final SeatsRepository seatsRepository;

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

        BigDecimal pricePerSeat = booking.getScreening().getTicketPrice();
        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(seats.size()));
        booking.setTicketPrice(totalPrice);

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

}
