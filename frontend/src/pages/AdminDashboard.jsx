
import { useEffect, useState, useCallback } from "react";
import { getAdminAnalytics } from "../services/adminApi";
import "./AdminDashboard.css";

function AdminDashboard() {

    const [analytics, setAnalytics] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const fetchAnalytics = useCallback(async () => {
        try {
            const data = await getAdminAnalytics();

            setError("");
            setAnalytics(data);

        } catch (err) {
            console.error("Failed to fetch admin analytics:", err);
            setError("Unable to load dashboard analytics.");

        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {

        let active = true;

        getAdminAnalytics()
            .then((data) => {
                if (active) {
                    setError("");
                    setAnalytics(data);
                }
            })
            .catch((err) => {
                console.error("Failed to fetch admin analytics:", err);

                if (active) {
                    setError("Unable to load dashboard analytics.");
                }
            })
            .finally(() => {
                if (active) {
                    setLoading(false);
                }
            });

        return () => {
            active = false;
        };

    }, []);

    const totalTickets = analytics.reduce(
        (sum, event) => sum + event.ticketsSold, 0
    );

    const totalRevenue = analytics.reduce(
        (sum, event) => sum + Number(event.revenue), 0
    );

    const totalSeats = analytics.reduce(
        (sum, event) => sum + event.totalSeats, 0
    );

    const occupiedSeats = analytics.reduce(
        (sum, event) => sum + event.occupiedSeats, 0
    );

    const occupancyRate = totalSeats === 0
        ? 0
        : (occupiedSeats / totalSeats) * 100;

    if (loading) {
        return <h2>Loading Admin Dashboard...</h2>;
    }

    if (error) {
        return (
            <div>
                <p>{error}</p>
                <button onClick={fetchAnalytics}>Retry</button>
            </div>
        );
    }

    return (
        <div className="admin-dashboard">

            <div className="admin-header">
                <div>
                    <h1>Admin Dashboard</h1>
                    <p>Monitor your event sales and performance.</p>
                </div>

                <button onClick={fetchAnalytics}>
                    Refresh
                </button>
            </div>

            <div className="admin-stats">

                <div className="stat-card">
                    <h3>Total Tickets Sold</h3>
                    <h2>{totalTickets}</h2>
                </div>

                <div className="stat-card">
                    <h3>Total Revenue</h3>
                    <h2>
                        ₹{totalRevenue.toLocaleString("en-IN")}
                    </h2>
                </div>

                <div className="stat-card">
                    <h3>Total Events</h3>
                    <h2>{analytics.length}</h2>
                </div>

                <div className="stat-card">
                    <h3>Overall Occupancy</h3>
                    <h2>{occupancyRate.toFixed(1)}%</h2>
                </div>

            </div>

            <div className="admin-events">
                <h2>Event Analytics</h2>

                <table>
                    <thead>
                    <tr>
                        <th>Event</th>
                        <th>Tickets Sold</th>
                        <th>Revenue</th>
                        <th>Available Seats</th>
                        <th>Occupancy</th>
                    </tr>
                    </thead>

                    <tbody>
                    {analytics.map((event) => (
                        <tr key={event.eventId}>
                            <td>{event.eventName}</td>
                            <td>{event.ticketsSold}</td>
                            <td>
                                ₹{Number(event.revenue).toLocaleString("en-IN")}
                            </td>
                            <td>{event.availableSeats}</td>
                            <td>{event.occupancyRate.toFixed(1)}%</td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                {analytics.length === 0 && (
                    <p>No events found.</p>
                )}
            </div>

        </div>
    );
}

export default AdminDashboard;
