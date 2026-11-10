package com.example.snapseat.seat;

import org.springframework.web.bind.annotation.*;

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