
package com.example.snapseat.booking;

import com.example.snapseat.booking.dto.BookingResponse;
import com.example.snapseat.event.SeatStatusChangedEvent;
import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatRepository;
import com.example.snapseat.seat.SeatStatus;
import com.example.snapseat.seat.SeatUpdate;
import com.example.snapseat.user.User;
import com.example.snapseat.user.UserRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository br;
    private final SeatRepository sr;
    private final UserRepository ur;
    private final ApplicationEventPublisher eventPublisher;

    public BookingService(
            BookingRepository br,
            SeatRepository sr,
            UserRepository ur,
            ApplicationEventPublisher eventPublisher
    ) {
        this.br = br;
        this.sr = sr;
        this.ur = ur;
        this.eventPublisher = eventPublisher;
    }

    // Reserve a seat
    @Transactional
    public Booking bookSeat(Long seatId, String email) {

        User u = ur.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Seat s = sr.findByIdForUpdate(seatId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Seat not found"
                ));

        OffsetDateTime now = OffsetDateTime.now();

        if (s.getStatus() == SeatStatus.BOOKED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Seat is already booked"
            );
        }

        if (s.getStatus() == SeatStatus.LOCKED &&
                s.getLockedUntil() != null &&
                s.getLockedUntil().isAfter(now)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Seat is currently locked"
            );
        }

        // Expire an older pending booking if its lock has expired
        if (s.getStatus() == SeatStatus.LOCKED &&
                (s.getLockedUntil() == null ||
                        !s.getLockedUntil().isAfter(now))) {

            Booking oldBooking = br.findBySeatIdAndStatus(
                    seatId, BookingStatus.PENDING
            ).orElse(null);

            if (oldBooking != null) {
                oldBooking.setStatus(BookingStatus.EXPIRED);
                br.saveAndFlush(oldBooking);
            }
        }

        // Lock the seat for five minutes
        s.setStatus(SeatStatus.LOCKED);
        s.setLockedBy(u);
        s.setLockedUntil(now.plusMinutes(5));

        sr.save(s);

        Booking booking = new Booking();
        booking.setUser(u);
        booking.setEvent(s.getEvent());
        booking.setSeat(s);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = br.save(booking);

        // Publish seat status change event
        publishSeatUpdate(s);

        return savedBooking;
    }

    // Restore the current user's active booking
    @Transactional(readOnly = true)
    public Optional<BookingResponse> getActiveBooking(String email) {

        OffsetDateTime now = OffsetDateTime.now();

        return br.findActiveBookings(
                        email,
                        BookingStatus.PENDING,
                        now
                )
                .stream()
                .findFirst()
                .map(b -> new BookingResponse(
                        b.getId(),
                        b.getSeat().getId(),
                        b.getEvent().getId(),
                        b.getStatus(),
                        b.getCreatedAt(),
                        b.getSeat().getLockedUntil()
                ));
    }

    // Mock payment and booking confirmation
    @Transactional
    public Booking payForBooking(Long bookingId, String email) {

        Long seatId = br.findSeatIdByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));

        // Lock the seat first
        Seat seat = sr.findByIdForUpdate(seatId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Seat not found"
                ));

        // Lock the booking
        Booking booking = br.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));

        // Verify booking ownership
        if (!booking.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot pay for another user's booking"
            );
        }

        // Prevent duplicate payment
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This booking is already confirmed"
            );
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This booking is not pending"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        // Verify reservation expiry
        if (seat.getLockedUntil() == null ||
                !seat.getLockedUntil().isAfter(now)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reservation expired. Please select the seat again."
            );
        }

        // Verify seat ownership
        if (seat.getStatus() != SeatStatus.LOCKED ||
                seat.getLockedBy() == null ||
                !seat.getLockedBy().getEmail().equals(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat is no longer reserved by you"
            );
        }

        // Confirm booking
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(now);

        // Mark seat as booked
        seat.setStatus(SeatStatus.BOOKED);

        // Remove temporary lock information
        seat.setLockedBy(null);
        seat.setLockedUntil(null);

        sr.save(seat);

        Booking confirmedBooking = br.save(booking);

        // Publish seat status change event
        publishSeatUpdate(seat);

        return confirmedBooking;
    }

    // Publish Spring application event
    private void publishSeatUpdate(Seat seat) {

        SeatUpdate update = new SeatUpdate(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getStatus(),
                seat.getEvent().getId()
        );

        eventPublisher.publishEvent(
                new SeatStatusChangedEvent(update)
        );
    }
}
