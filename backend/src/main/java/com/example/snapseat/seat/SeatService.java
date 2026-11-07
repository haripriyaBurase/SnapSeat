package com.example.snapseat.seat;


import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    private final SeatRepository sr;

    public SeatService(SeatRepository sr) {
        this.sr = sr;
    }

    public List<Seat> getSeatsByEvent(Long eventId){
        return sr.findByEventId(eventId);
    }
}
