
import { useEffect, useState } from "react";

import { getSeatsByEventId } from "../services/eventService";
import SeatMap from "../components/SeatMap";
import useSeatUpdates from "../hooks/useSeatUpdates";

import {
    reserveSeat,
    getMyActiveBooking,
    payForBooking
} from "../services/bookingService";

import useCountdown from "../hooks/useCountdown";

function EventDetails() {

    const eventId = 1;

    // Seat states
    const [seats, setSeats] = useState([]);
    const [selectedSeat, setSelectedSeat] = useState(null);

    // Booking states
    const [booking, setBooking] = useState(null);

    // UI states
    const [loading, setLoading] = useState(true);
    const [reserving, setReserving] = useState(false);
    const [paying, setPaying] = useState(false);
    const [error, setError] = useState("");

    // Countdown
    const { formattedTime, expired } =
        useCountdown(booking?.lockedUntil);

    // WebSocket updates
    useSeatUpdates(eventId, setSeats);

    // Load seats and restore active booking
    useEffect(() => {

        const initializePage = async () => {

            try {

                setLoading(true);
                setError("");

                const seatData = await getSeatsByEventId(eventId);
                setSeats(seatData);

                const activeBooking = await getMyActiveBooking();

                if (activeBooking) {

                    setBooking(activeBooking);

                    const matchingSeat = seatData.find(
                        seat => seat.id === activeBooking.seatId
                    );

                    if (matchingSeat) {
                        setSelectedSeat(matchingSeat);
                    }
                }

            } catch (err) {

                console.error(err);
                setError("Unable to load seats or restore your booking.");

            } finally {

                setLoading(false);

            }
        };

        void initializePage();

    }, []);

    // Reserve a seat
    const handleSeatSelect = async (seat) => {

        if (
            seat.status !== "AVAILABLE" ||
            reserving ||
            paying ||
            (booking?.status === "PENDING" && !expired)
        ) {
            return;
        }

        try {

            setReserving(true);
            setError("");

            const response = await reserveSeat(seat.id);

            setSelectedSeat(seat);
            setBooking(response);

        } catch (err) {

            console.error(err);

            setError(
                err.response?.data?.message ||
                "Unable to reserve this seat. It may already be booked or locked."
            );

        } finally {

            setReserving(false);

        }
    };

    // Process mock payment
    const handlePayment = async () => {

        if (!booking || booking.status !== "PENDING" || expired) {
            return;
        }

        try {

            setPaying(true);
            setError("");

            const response = await payForBooking(booking.id);

            // Update booking details
            setBooking(response);

            // Immediately update the local seat map
            setSeats(previousSeats =>
                previousSeats.map(seat =>
                    seat.id === response.seatId
                        ? { ...seat, status: "BOOKED" }
                        : seat
                )
            );

            // Update selected seat status too
            setSelectedSeat(previousSeat => ({
                ...previousSeat,
                status: "BOOKED"
            }));

        } catch (err) {

            console.error(err);

            setError(
                err.response?.data?.message ||
                "Payment failed. Please try again."
            );

        } finally {

            setPaying(false);

        }
    };

    // Loading screen
    if (loading) {
        return <h2>Loading seats...</h2>;
    }

    if (error && seats.length === 0) {
        return <h2>{error}</h2>;
    }

    return (

        <div>

            <h1>SnapSeat Live Concert</h1>

            <p>Pune Convention Centre</p>

            {error && (
                <p style={{ color: "red" }}>
                    {error}
                </p>
            )}

            {/* Seat Map */}
            <SeatMap
                seats={seats}
                onSelect={handleSeatSelect}
            />

            {reserving && (
                <p>Reserving your seat...</p>
            )}

            {/* Checkout */}
            {booking && selectedSeat && (

                <div style={{
                    border: "1px solid #ccc",
                    padding: "20px",
                    margin: "20px",
                    borderRadius: "10px"
                }}>

                    <h2>
                        {booking.status === "CONFIRMED"
                            ? "Booking Confirmed!"
                            : "Checkout"}
                    </h2>

                    <p>
                        Seat: {selectedSeat.seatNumber}
                    </p>

                    <p>
                        Price: ₹{selectedSeat.price}
                    </p>

                    <p>
                        Tier: {selectedSeat.tier}
                    </p>

                    <p>
                        Booking ID: {booking.id}
                    </p>

                    {/* Pending booking */}
                    {booking.status === "PENDING" && (

                        <>

                            <p style={{ color: "green" }}>
                                Seat reserved successfully!
                            </p>

                            <h3>
                                Time remaining: {formattedTime}
                            </h3>

                            {expired ? (

                                <p style={{ color: "red" }}>
                                    Your reservation has expired.
                                    Please select another available seat.
                                </p>

                            ) : (

                                <button
                                    onClick={handlePayment}
                                    disabled={paying}
                                    style={{
                                        padding: "12px 25px",
                                        backgroundColor: "#16a34a",
                                        color: "white",
                                        border: "none",
                                        borderRadius: "8px",
                                        cursor: paying ? "not-allowed" : "pointer",
                                        fontSize: "16px",
                                        fontWeight: "bold"
                                    }}
                                >
                                    {paying
                                        ? "Processing Payment..."
                                        : `Pay Now - ₹${selectedSeat.price}`}
                                </button>

                            )}

                        </>

                    )}

                    {/* Confirmed booking */}
                    {booking.status === "CONFIRMED" && (

                        <div style={{
                            backgroundColor: "#dcfce7",
                            padding: "15px",
                            borderRadius: "8px",
                            marginTop: "15px"
                        }}>

                            <h3 style={{ color: "#166534" }}>
                                Payment Successful!
                            </h3>

                            <p>
                                Your seat has been booked successfully.
                            </p>

                            <p>
                                Booking Status: CONFIRMED
                            </p>

                        </div>

                    )}

                </div>

            )}

        </div>
    );
}

export default EventDetails;
