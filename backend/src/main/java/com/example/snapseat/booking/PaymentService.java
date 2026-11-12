package com.example.snapseat.booking;

import com.example.snapseat.booking.dto.BookingReceipt;
import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatRepository;
import com.example.snapseat.seat.SeatStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
public class PaymentService {

    private final BookingRepository br;
    private final SeatRepository sr;

    public PaymentService(BookingRepository br, SeatRepository sr) {
        this.br = br;
        this.sr = sr;
    }
    @Transactional
    public BookingReceipt confirmPayment(Long bookingId, String email) {

        // 1. Find the seat associated with this booking
        Long seatId = br.findSeatIdByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));

        // 2. Lock the seat
        Seat seat = sr.findByIdForUpdate(seatId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Seat not found"
                ));

        // 3. Lock and retrieve the booking
        Booking booking = br.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));
        if (!booking.getSeat().getId().equals(seatId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking seat mismatch"
            );
        }
        // 4. Verify booking ownership
        if (!booking.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You cannot confirm this booking"
            );
        }

        // 5. Check booking status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking is not pending"
            );
        }

        // 6. Check seat status and ownership
        if (seat.getStatus() != SeatStatus.LOCKED ||
                seat.getLockedBy() == null ||
                !seat.getLockedBy().getEmail().equals(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Seat is not locked by you"
            );
        }

        // 7. Check expiration
        if (seat.getLockedUntil() == null ||
                !seat.getLockedUntil().isAfter(OffsetDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Seat reservation has expired"
            );
        }

        // 8. Confirm booking
        seat.setStatus(SeatStatus.BOOKED);
        seat.setLockedBy(null);
        seat.setLockedUntil(null);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(OffsetDateTime.now());

        return new BookingReceipt(
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
    }
}