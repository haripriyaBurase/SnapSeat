package com.example.snapseat.booking.dto;

import com.example.snapseat.booking.BookingStatus;

import java.time.OffsetDateTime;

public record BookingResponse(
        Long id,
        Long seatId,
        Long eventId,
        BookingStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime lockedUntil
) {}