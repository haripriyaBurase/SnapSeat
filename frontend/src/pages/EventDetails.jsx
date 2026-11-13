
import { useEffect, useState } from "react";
import { getSeatsByEventId } from "../services/eventService";
import SeatMap from '../components/SeatMap';
import useSeatUpdates from "../hooks/useSeatUpdates";

function EventDetails() {

    const eventId = 1;

    const [seats, setSeats] = useState([]);
    useSeatUpdates(eventId, setSeats);
    const [selectedSeat, setSelectedSeat] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        const fetchSeats = async () => {
            try {
                const data = await getSeatsByEventId(eventId);
                setSeats(data);
            } catch (err) {
                console.error(err);
                setError("Unable to load seats.");
            } finally {
                setLoading(false);
            }
        };

        fetchSeats();

    }, []);

    const handleSeatSelect = (seat) => {
        setSelectedSeat(seat);
    };

    if (loading) {
        return <h2>Loading seats...</h2>;
    }

    if (error) {
        return <h2>{error}</h2>;
    }

    return (
        <div>
            <h1>SnapSeat Live Concert</h1>
            <p>Pune Convention Centre</p>

            <SeatMap
                seats={seats}
                onSelect={handleSeatSelect}
            />

            {selectedSeat && (
                <div>
                    <h3>Selected Seat</h3>
                    <p>Seat: {selectedSeat.seatNumber}</p>
                    <p>Price: ₹{selectedSeat.price}</p>
                    <p>Tier: {selectedSeat.tier}</p>
                </div>
            )}
        </div>
    );
}

export default EventDetails;
