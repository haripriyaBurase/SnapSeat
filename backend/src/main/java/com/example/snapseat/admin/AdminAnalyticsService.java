package com.example.snapseat.admin;

import com.example.snapseat.booking.BookingStatus;
import com.example.snapseat.seat.SeatStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminAnalyticsService {

    private final AdminAnalyticsRepository analyticsRepository;

    public AdminAnalyticsService(AdminAnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminEventAnalyticsResponse> getEventAnalytics() {

        List<AdminEventAnalyticsProjection> results =
                analyticsRepository.findEventAnalytics(
                        SeatStatus.BOOKED,
                        BookingStatus.CONFIRMED
                );

        return results.stream()
                .map(result -> {

                    long totalSeats = result.getTotalSeats() == null
                            ? 0 : result.getTotalSeats();

                    long ticketsSold = result.getTicketsSold() == null
                            ? 0 : result.getTicketsSold();

                    long occupiedSeats = result.getOccupiedSeats() == null
                            ? 0 : result.getOccupiedSeats();

                    long availableSeats = Math.max(0, totalSeats - occupiedSeats);

                    double occupancyRate = totalSeats == 0
                            ? 0.0
                            : (occupiedSeats * 100.0) / totalSeats;

                    BigDecimal revenue = result.getRevenue() == null
                            ? BigDecimal.ZERO
                            : result.getRevenue();

                    return new AdminEventAnalyticsResponse(
                            result.getEventId(),
                            result.getEventName(),
                            (int) totalSeats,
                            ticketsSold,
                            occupiedSeats,
                            availableSeats,
                            Math.round(occupancyRate * 100.0) / 100.0,
                            revenue
                    );
                })
                .toList();
    }
}