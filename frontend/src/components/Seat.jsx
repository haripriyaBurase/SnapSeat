
function Seat({ seat, onSelect }) {

    const getSeatColor = () => {
        switch (seat.status) {
            case "AVAILABLE":
                return "#22c55e";

            case "LOCKED":
                return "#737373";

            case "BOOKED":
                return "#ef4444";

            default:
                return "#d1d5db";
        }
    };

    return (
        <button
            onClick={() => onSelect(seat)}
            disabled={seat.status !== "AVAILABLE"}
            style={{
                backgroundColor: getSeatColor(),
                color: "white",
                padding: "12px",
                margin: "6px",
                border: "none",
                borderRadius: "8px",
                cursor: seat.status === "AVAILABLE"
                    ? "pointer"
                    : "not-allowed"
            }}
        >
            {seat.seatNumber}
        </button>
    );
}

export default Seat;
