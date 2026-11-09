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

    @PostMapping("/reserve/{seatId}")
    public ResponseEntity<BookingResponse> reserveSeat(
            @PathVariable Long seatId,
            Authentication auth) {
        System.out.println("RESERVATION CONTROLLER REACHED!");
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

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}