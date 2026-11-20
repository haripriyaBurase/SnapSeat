
import { useState } from "react";

import EventDetails from "./pages/EventDetails";
import MyBookings from "./pages/MyBookings";
import AdminDashboard from "./pages/AdminDashboard";

function App() {

    const [activePage, setActivePage] = useState("events");

    return (
        <div>

            <nav>
                <button onClick={() => setActivePage("events")}>
                    Events
                </button>

                <button onClick={() => setActivePage("bookings")}>
                    My Bookings
                </button>

                <button onClick={() => setActivePage("admin")}>
                    Admin Dashboard
                </button>
            </nav>

            {activePage === "events" && <EventDetails />}

            {activePage === "bookings" && <MyBookings />}

            {activePage === "admin" && <AdminDashboard />}

        </div>
    );
}

export default App;
