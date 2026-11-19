import EventDetails from "./pages/EventDetails";
import { useState } from "react";
import MyBookings from "./pages/MyBookings";

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
          </nav>

          {activePage === "events" ? (
              <EventDetails />
          ) : (
              <MyBookings />
          )}
      </div>

  );

}

export default App;