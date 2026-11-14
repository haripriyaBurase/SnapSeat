
package com.example.snapseat.booking;

import com.example.snapseat.booking.dto.BookingResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bs;

    public BookingController(BookingService bs) {
        this.bs = bs;
    }

    // Reserve a seat
    @PostMapping("/reserve/{seatId}")
    public ResponseEntity<BookingResponse> reserveSeat(
            @PathVariable Long seatId,
            Authentication auth
    ) {

        String email = auth.getName();

        Booking b = bs.bookSeat(seatId, email);

        BookingResponse response = new BookingResponse(
                b.getId(),
                b.getSeat().getId(),
                b.getEvent().getId(),
                b.getStatus(),
                b.getCreatedAt(),
                b.getSeat().getLockedUntil()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Restore the logged-in user's active booking
    @GetMapping("/my-active")
    public ResponseEntity<BookingResponse> getMyActiveBooking(
            Authentication auth
    ) {

        String email = auth.getName();

        return bs.getActiveBooking(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // Mock payment
    @PostMapping("/{bookingId}/pay")
    public ResponseEntity<BookingResponse> payForBooking(
            @PathVariable Long bookingId,
            Authentication auth
    ) {

        String email = auth.getName();

        Booking b = bs.payForBooking(bookingId, email);

        BookingResponse response = new BookingResponse(
                b.getId(),
                b.getSeat().getId(),
                b.getEvent().getId(),
                b.getStatus(),
                b.getCreatedAt(),
                b.getSeat().getLockedUntil()
        );

        return ResponseEntity.ok(response);
    }
}
