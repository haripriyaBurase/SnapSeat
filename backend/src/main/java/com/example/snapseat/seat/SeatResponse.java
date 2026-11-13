
package com.example.snapseat.seat;

import java.math.BigDecimal;

public record SeatResponse(
        Long id,
        String seatNumber,
        SeatTier tier,
        BigDecimal price,
        SeatStatus status
) {}
