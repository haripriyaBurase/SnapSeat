
import Seat from "./Seat";

function SeatMap({ seats, onSelect }) {

    return (
        <div style={{ padding: "20px" }}>

            <h2>Select Your Seats</h2>

            {/* Screen */}
            <div style={{
                backgroundColor: "#d1d5db",
                padding: "10px",
                textAlign: "center",
                marginBottom: "30px",
                borderRadius: "8px",
                fontWeight: "bold"
            }}>
                SCREEN
            </div>

            {/* Seat Layout */}
            <div style={{
                display: "flex",
                flexWrap: "wrap",
                justifyContent: "center",
                maxWidth: "650px",
                margin: "auto"
            }}>
                {seats.map((seat) => (
                    <Seat
                        key={seat.id}
                        seat={seat}
                        onSelect={onSelect}
                    />
                ))}
            </div>

            {/* Legend */}
            <div style={{
                display: "flex",
                justifyContent: "center",
                gap: "20px",
                marginTop: "30px"
            }}>
                <span>🟢 Available</span>
                <span>⚪ Locked</span>
                <span>🔴 Booked</span>
            </div>

        </div>
    );
}

export default SeatMap;
