package com.example.snapseat.booking.dto;

import com.example.snapseat.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MyBookingResponse(
        Long bookingId,
        String eventTitle,
        OffsetDateTime eventDate,
        String venue,
        String seatNumber,
        String seatTier,
        BigDecimal price,
        BookingStatus status,
        OffsetDateTime bookedAt
) {}
