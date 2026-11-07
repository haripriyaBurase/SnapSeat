package com.example.snapseat.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService es;

    public EventController(EventService es) {
        this.es = es;
    }

    @GetMapping
    public List<Event>getAllEvent(){
        return es.getAllEvents();
    }
}
