
import api from "./api";

export const reserveSeat = async (seatId) => {
    const response = await api.post(`/api/bookings/reserve/${seatId}`);
    return response.data;
};

export const getMyActiveBooking = async () => {
    const response = await api.get("/api/bookings/my-active");

    if (response.status === 204) {
        return null;
    }

    return response.data;
};

export const payForBooking = async (bookingId) => {
    const response = await api.post(`/api/bookings/${bookingId}/pay`);
    return response.data;
};
