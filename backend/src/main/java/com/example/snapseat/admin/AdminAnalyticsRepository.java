package com.example.snapseat.admin;

import com.example.snapseat.booking.Booking;
import com.example.snapseat.booking.BookingStatus;
import com.example.snapseat.event.Event;
import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminAnalyticsRepository
        extends JpaRepository<Event, Long> {

    @Query("""
        SELECT
            e.id AS eventId,
            e.title AS eventName,
            e.totalSeats AS totalSeats,

            (SELECT COUNT(b.id)
             FROM Booking b
             WHERE b.event = e
             AND b.status = :confirmedStatus
            ) AS ticketsSold,

            (SELECT COUNT(s.id)
             FROM Seat s
             WHERE s.event = e
             AND s.status = :bookedStatus
            ) AS occupiedSeats,

            (SELECT COALESCE(SUM(s2.price), 0)
             FROM Booking b2
             JOIN b2.seat s2
             WHERE b2.event = e
             AND b2.status = :confirmedStatus
            ) AS revenue

        FROM Event e
        """)
    List<AdminEventAnalyticsProjection> findEventAnalytics(
            @Param("bookedStatus") SeatStatus bookedStatus,
            @Param("confirmedStatus") BookingStatus confirmedStatus
    );
}