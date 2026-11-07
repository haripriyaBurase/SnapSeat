
package com.example.snapseat.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    private EventRepository er;
    private EventService es;

    @BeforeEach
    void setUp() {
        er = mock(EventRepository.class);
        es = new EventService(er);
    }

    @Test
    void getAllEventsSuccess() {
        Event e = new Event();

        when(er.findAll()).thenReturn(List.of(e));

        List<Event> res = es.getAllEvents();

        assertEquals(1, res.size());
        verify(er).findAll();
    }

    @Test
    void getAllEventsEmpty() {
        when(er.findAll()).thenReturn(List.of());

        List<Event> res = es.getAllEvents();

        assertTrue(res.isEmpty());
        verify(er).findAll();
    }
}
