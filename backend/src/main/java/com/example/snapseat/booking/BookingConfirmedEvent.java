
package com.example.snapseat.booking;

public class BookingConfirmedEvent {

    private final Long bookingId;

    public BookingConfirmedEvent(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getBookingId() {
        return bookingId;
    }
}
