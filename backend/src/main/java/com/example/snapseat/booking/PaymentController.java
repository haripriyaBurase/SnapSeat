package com.example.snapseat.booking;

import com.example.snapseat.booking.dto.BookingReceipt;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class PaymentController {

    private final PaymentService ps;

    public PaymentController(PaymentService ps) {
        this.ps = ps;
    }
    @PostMapping("/{bookingId}/confirm-payment")
    public ResponseEntity<BookingReceipt> confirmPayment(
            @PathVariable Long bookingId,
            Authentication auth) {

        String email = auth.getName();

        BookingReceipt receipt =
                ps.confirmPayment(bookingId, email);

        return ResponseEntity.ok(receipt);
    }
}