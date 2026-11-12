package com.example.snapseat.booking.dto;

import com.example.snapseat.booking.BookingStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BookingReceipt (
    Long bookingId,
    Long eventId,
    String eventName,
    String venue,
    Long seatId,
    String seatNumber,
    BigDecimal amount,
    BookingStatus bookingStatus,
    String paymentStatus,
    OffsetDateTime bookingTime
){}
