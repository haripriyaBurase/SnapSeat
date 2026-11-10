package com.example.snapseat.config;

import com.example.snapseat.event.Event;
import com.example.snapseat.event.EventRepository;
import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatRepository;
import com.example.snapseat.seat.SeatStatus;
import com.example.snapseat.seat.SeatTier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            EventRepository eventRepository,
            SeatRepository seatRepository) {

        return args -> {

            // Prevent duplicate test data
            if (eventRepository.count() > 0) {
                System.out.println("Test data already exists!");
                return;
            }

            // 1. Create Event
            Event event = new Event();
            event.setTitle("SnapSeat Live Concert");
            event.setDescription("Sample event for testing reservations");
            event.setEventDate(
                    OffsetDateTime.now().plusDays(30)
            );
            event.setVenue("Pune Convention Centre");
            event.setTotalSeats(20);

            event = eventRepository.save(event);

            // 2. Create Seats
            for (int i = 1; i <= 20; i++) {

                Seat seat = new Seat();
                seat.setEvent(event);

                seat.setSeatNumber("A" + i);

                if (i <= 6) {
                    seat.setTier(SeatTier.REGULAR);
                    seat.setPrice(new BigDecimal("200.00"));
                } else if (i <= 14) {
                    seat.setTier(SeatTier.PREMIUM);
                    seat.setPrice(new BigDecimal("350.00"));
                } else {
                    seat.setTier(SeatTier.VIP);
                    seat.setPrice(new BigDecimal("500.00"));
                }

                seat.setStatus(SeatStatus.AVAILABLE);

                seatRepository.save(seat);
            }

            System.out.println("Event and 20 seats created!");
        };
    }
}