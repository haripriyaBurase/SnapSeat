
package com.example.snapseat.seat;

public record SeatUpdate(
        Long seatId,
        String seatNumber,
        SeatStatus status,
        Long eventId
) {}
