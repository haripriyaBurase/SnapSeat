
package com.example.snapseat.listener;

import com.example.snapseat.event.SeatStatusChangedEvent;
import com.example.snapseat.seat.SeatUpdate;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class SeatEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public SeatEventListener(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleSeatStatusChanged(
            SeatStatusChangedEvent event
    ) {

        SeatUpdate update = event.getSeatUpdate();

        Long eventId = update.eventId();

        messagingTemplate.convertAndSend(
                "/topic/events/" + eventId + "/seats",
                update
        );

        System.out.println(
                "WebSocket update sent for seat: "
                        + update.seatNumber()
                        + " | Status: "
                        + update.status()
        );
    }
}
