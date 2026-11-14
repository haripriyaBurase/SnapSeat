
package com.example.snapseat.booking;

import com.example.snapseat.booking.dto.BookingResponse;
import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatRepository;
import com.example.snapseat.seat.SeatStatus;
import com.example.snapseat.seat.SeatUpdate;
import com.example.snapseat.user.User;
import com.example.snapseat.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository br;
    private final SeatRepository sr;
    private final UserRepository ur;
    private final SimpMessagingTemplate messagingTemplate;

    public BookingService(
            BookingRepository br,
            SeatRepository sr,
            UserRepository ur,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.br = br;
        this.sr = sr;
        this.ur = ur;
        this.messagingTemplate = messagingTemplate;
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

        publishSeatUpdateAfterCommit(s);

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

        // Find the seat ID before acquiring locks.
        Long seatId = br.findSeatIdByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));

        // Lock the seat first to maintain the same lock order
        // as the reservation process.
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

        // Verify that the reservation has not expired
        if (seat.getLockedUntil() == null ||
                !seat.getLockedUntil().isAfter(now)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reservation expired. Please select the seat again."
            );
        }

        // Verify that the seat is still locked by this user
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

        // Notify other users after the transaction commits
        publishSeatUpdateAfterCommit(seat);

        return confirmedBooking;
    }

    // Broadcast seat updates only after successful database commit
    private void publishSeatUpdateAfterCommit(Seat seat) {

        SeatUpdate update = new SeatUpdate(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getStatus(),
                seat.getEvent().getId()
        );

        Long eventId = seat.getEvent().getId();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend(
                                "/topic/events/" + eventId + "/seats",
                                update
                        );
                    }
                }
        );
    }
}
