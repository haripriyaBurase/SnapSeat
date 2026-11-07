package com.example.snapseat.event;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class EventService {
    private final EventRepository er;

    public EventService(EventRepository er) {
        this.er = er;
    }

    public List<Event> getAllEvents() {
        return er.findAll();
    }
}
