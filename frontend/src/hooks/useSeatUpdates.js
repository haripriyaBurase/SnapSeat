
import { useEffect } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

function useSeatUpdates(eventId, setSeats) {

    useEffect(() => {

        const client = new Client({

            webSocketFactory: () =>
                new SockJS("http://localhost:8080/ws"),

            reconnectDelay: 5000,

            onConnect: () => {

                console.log("WebSocket connected!");

                client.subscribe(
                    `/topic/events/${eventId}/seats`,
                    (message) => {

                        const update = JSON.parse(message.body);

                        console.log("Seat update received:", update);

                        setSeats((currentSeats) =>
                            currentSeats.map((seat) =>
                                seat.id === update.seatId
                                    ? { ...seat, status: update.status }
                                    : seat
                            )
                        );
                    }
                );
            },

            onStompError: (frame) => {
                console.error("STOMP error:", frame);
            }
        });

        client.activate();

        return () => {
            client.deactivate();
        };

    }, [eventId, setSeats]);
}

export default useSeatUpdates;
