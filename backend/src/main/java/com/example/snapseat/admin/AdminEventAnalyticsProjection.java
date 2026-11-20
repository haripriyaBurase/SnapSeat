package com.example.snapseat.admin;

import java.math.BigDecimal;

public interface AdminEventAnalyticsProjection {

    Long getEventId();

    String getEventName();

    Integer getTotalSeats();

    Long getTicketsSold();

    Long getOccupiedSeats();

    BigDecimal getRevenue();
}