
package com.example.snapseat.booking;

import com.example.snapseat.booking.dto.BookingReceipt;
import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatRepository;
import com.example.snapseat.seat.SeatStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

@Service
public class PaymentService {

    private final BookingRepository br;
    private final SeatRepository sr;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(
            BookingRepository br,
            SeatRepository sr,
            ApplicationEventPublisher eventPublisher) {

        this.br = br;
        this.sr = sr;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BookingReceipt confirmPayment(Long bookingId, String email) {
        System.out.println("========== PAYMENT SERVICE CALLED ==========");
        Long seatId = br.findSeatIdByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));

        Seat seat = sr.findByIdForUpdate(seatId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Seat not found"
                ));

        Booking booking = br.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));

        if (!booking.getSeat().getId().equals(seatId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking seat mismatch"
            );
        }

        if (!booking.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot confirm this booking"
            );
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking is not pending"
            );
        }

        if (seat.getStatus() != SeatStatus.LOCKED ||
                seat.getLockedBy() == null ||
                !seat.getLockedBy().getEmail().equals(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Seat is not locked by you"
            );
        }

        if (seat.getLockedUntil() == null ||
                !seat.getLockedUntil().isAfter(OffsetDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat reservation has expired"
            );
        }

        // Confirm booking
        seat.setStatus(SeatStatus.BOOKED);
        seat.setLockedBy(null);
        seat.setLockedUntil(null);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(OffsetDateTime.now());

        BookingReceipt receipt = new BookingReceipt(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getEvent().getTitle(),
                booking.getEvent().getVenue(),
                seat.getId(),
                seat.getSeatNumber(),
                seat.getPrice(),
                booking.getStatus(),
                "SUCCESS",
                booking.getUpdatedAt()
        );
        System.out.println("========== EVENT PUBLISHING ==========");

        // Publish event for background processing
        eventPublisher.publishEvent(
                new BookingConfirmedEvent(booking.getId())
        );

        return receipt;
    }
}
