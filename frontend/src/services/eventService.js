
import api from "./api";

// Fetch all events
export const getAllEvents = async () => {
    const response = await api.get("/api/events");
    return response.data;
};

// Fetch seats for a particular event
export const getSeatsByEventId = async (eventId) => {
    const response = await api.get(`/api/events/${eventId}/seats`);
    return response.data;
};
