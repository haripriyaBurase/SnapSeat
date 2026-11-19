
import { useEffect, useState } from "react";
import { getMyBookings } from "../services/bookingService";
import "./MyBookings.css";

function MyBookings() {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [activeFilter, setActiveFilter] = useState("ALL");

    useEffect(() => {
        const fetchBookings = async () => {
            try {
                setLoading(true);

                const data = await getMyBookings();
                setBookings(data);

            } catch (err) {
                console.error(err);
                setError("Unable to fetch your bookings.");
            } finally {
                setLoading(false);
            }
        };

        fetchBookings();
    }, []);

    if (loading) {
        return <h2>Loading your bookings...</h2>;
    }

    if (error) {
        return <h2>{error}</h2>;
    }

    if (bookings.length === 0) {
        return (
            <div className="my-bookings-page">
                <h1>My Bookings</h1>
                <h2>No bookings yet!</h2>
                <p>
                    You haven't booked any tickets.
                    Explore events and book your first ticket.
                </p>
            </div>
        );
    }

    // Calculate booking summary
    const confirmedBookings = bookings.filter(
        (booking) => booking.status === "CONFIRMED"
    );

    const totalSpent = confirmedBookings.reduce(
        (total, booking) => total + Number(booking.price),
        0
    );

    // Categorize bookings
    const now = new Date();

    const upcomingBookings = confirmedBookings.filter(
        (booking) => new Date(booking.eventDate) > now
    );

    const pastBookings = confirmedBookings.filter(
        (booking) => new Date(booking.eventDate) <= now
    );

    const pendingBookings = bookings.filter(
        (booking) => booking.status === "PENDING"
    );

    // Select bookings according to active filter
    let filteredBookings = bookings;

    if (activeFilter === "UPCOMING") {
        filteredBookings = upcomingBookings;
    } else if (activeFilter === "PAST") {
        filteredBookings = pastBookings;
    } else if (activeFilter === "PENDING") {
        filteredBookings = pendingBookings;
    }

    return (
        <div className="my-bookings-page">

            <h1>My Bookings</h1>

            {/* Booking Summary */}
            <div className="booking-summary">

                <div className="summary-card">
                    <h3>Total Bookings</h3>
                    <p>{bookings.length}</p>
                </div>

                <div className="summary-card">
                    <h3>Confirmed Tickets</h3>
                    <p>{confirmedBookings.length}</p>
                </div>

                <div className="summary-card">
                    <h3>Total Spent</h3>
                    <p>₹{totalSpent.toLocaleString("en-IN")}</p>
                </div>

            </div>

            {/* Booking Filters */}
            <div className="booking-filters">

                <button
                    onClick={() => setActiveFilter("ALL")}
                    className={activeFilter === "ALL" ? "active" : ""}
                >
                    All
                </button>

                <button
                    onClick={() => setActiveFilter("UPCOMING")}
                    className={activeFilter === "UPCOMING" ? "active" : ""}
                >
                    Upcoming
                </button>

                <button
                    onClick={() => setActiveFilter("PAST")}
                    className={activeFilter === "PAST" ? "active" : ""}
                >
                    Past
                </button>

                <button
                    onClick={() => setActiveFilter("PENDING")}
                    className={activeFilter === "PENDING" ? "active" : ""}
                >
                    Pending
                </button>

            </div>

            {/* Booking Cards */}
            <div>

                {filteredBookings.length === 0 ? (

                    <div className="no-bookings">
                        <h2>
                            No {activeFilter.toLowerCase()} bookings found.
                        </h2>

                        <p>
                            Your bookings will appear here when available.
                        </p>
                    </div>

                ) : (

                    filteredBookings.map((booking) => (

                        <div
                            className="booking-card"
                            key={booking.bookingId}
                        >

                            <h2>{booking.eventTitle}</h2>

                            <p>Venue: {booking.venue}</p>

                            <p>
                                Event Date:{" "}
                                {new Date(
                                    booking.eventDate
                                ).toLocaleString()}
                            </p>

                            <hr />

                            <p>
                                Booking ID: #{booking.bookingId}
                            </p>

                            <p>Seat: {booking.seatNumber}</p>

                            <p>Tier: {booking.seatTier}</p>

                            <p>Ticket Price: ₹{booking.price}</p>

                            <p>
                                Status:{" "}
                                <span
                                    className={`booking-status ${
                                        booking.status === "CONFIRMED"
                                            ? "status-confirmed"
                                            : booking.status === "PENDING"
                                                ? "status-pending"
                                                : "status-expired"
                                    }`}
                                >
                                    {booking.status}
                                </span>
                            </p>

                            <p>
                                Booked At:{" "}
                                {new Date(
                                    booking.bookedAt
                                ).toLocaleString()}
                            </p>

                        </div>

                    ))

                )}

            </div>

        </div>
    );
}

export default MyBookings;
