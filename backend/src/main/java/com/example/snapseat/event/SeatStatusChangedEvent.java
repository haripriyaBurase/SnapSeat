
package com.example.snapseat.event;

import com.example.snapseat.seat.SeatUpdate;

public class SeatStatusChangedEvent {

    private final SeatUpdate seatUpdate;

    public SeatStatusChangedEvent(SeatUpdate seatUpdate) {
        this.seatUpdate = seatUpdate;
    }

    public SeatUpdate getSeatUpdate() {
        return seatUpdate;
    }
}
