package com.example.snapseat.admin;

import java.math.BigDecimal;

public record AdminEventAnalyticsResponse(
        Long eventId,
        String eventName,
        Integer totalSeats,
        Long ticketsSold,
        Long occupiedSeats,
        Long availableSeats,
        Double occupancyRate,
        BigDecimal revenue
) {
}