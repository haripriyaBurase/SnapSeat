package com.example.snapseat.seat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/seats")
public class SeatController {
    private final SeatService ss;
    public SeatController(SeatService ss) {
        this.ss = ss;
    }

    @GetMapping
    public List<Seat> getSeats(@PathVariable Long eventId) {
        return ss.getSeatsByEvent(eventId);
    }
}
