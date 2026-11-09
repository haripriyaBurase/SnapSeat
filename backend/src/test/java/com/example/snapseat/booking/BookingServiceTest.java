package com.example.snapseat.booking;

import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatRepository;
import com.example.snapseat.seat.SeatStatus;
import com.example.snapseat.user.User;
import com.example.snapseat.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository br;

    @Mock
    private SeatRepository sr;

    @Mock
    private UserRepository ur;

    @Mock
    private User u;

    @Mock
    private Seat s;

    @InjectMocks
    private BookingService bs;

    //Book available seat correctly
    @Test
    void bookAvailableSeat() {
        // Arrange
        Long seatId = 1L;
        String email = "test@example.com";

        User u = new User();
        Seat s = new Seat();

        s.setStatus(SeatStatus.AVAILABLE);

        when(ur.findByEmail(email))
                .thenReturn(Optional.of(u));

        when(sr.findByIdForUpdate(seatId))
                .thenReturn(Optional.of(s));

        when(br.save(any(Booking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        Booking b = bs.bookSeat(seatId, email);

        // Assert
        assertNotNull(b);
        assertEquals(BookingStatus.PENDING, b.getStatus());

        assertEquals(SeatStatus.LOCKED, s.getStatus());
        assertEquals(u, s.getLockedBy());
        assertNotNull(s.getLockedUntil());

        assertTrue(s.getLockedUntil().isAfter(OffsetDateTime.now()));
        assertEquals(u, b.getUser());
        assertEquals(s, b.getSeat());

        verify(sr).save(s);
        verify(br).save(any(Booking.class));
    }

    //Reject already booked seat
    @Test
    void rejectAlreadyBookedSeat() {

        Long seatId = 1L;
        String email = "test@example.com";

        User u = new User();
        Seat s = new Seat();

        s.setStatus(SeatStatus.BOOKED);

        when(ur.findByEmail(email))
                .thenReturn(Optional.of(u));

        when(sr.findByIdForUpdate(seatId))
                .thenReturn(Optional.of(s));

        assertThrows(RuntimeException.class,
                () -> bs.bookSeat(seatId, email));

        verify(br, never()).save(any(Booking.class));
        verify(sr, never()).save(any(Seat.class));
    }

    //Reject the seat locked by another user
    @Test
    void rejectSeatLockedByAnotherUser() {

        Long seatId = 1L;
        String email = "test@example.com";

        User u = new User();
        Seat s = new Seat();

        s.setStatus(SeatStatus.LOCKED);
        s.setLockedUntil(OffsetDateTime.now().plusMinutes(3));

        when(ur.findByEmail(email))
                .thenReturn(Optional.of(u));

        when(sr.findByIdForUpdate(seatId))
                .thenReturn(Optional.of(s));

        assertThrows(RuntimeException.class,
                () -> bs.bookSeat(seatId, email));

        verify(br, never()).save(any(Booking.class));
        verify(sr, never()).save(any(Seat.class));
    }

    //Allow Booking after lock expires

    @Test
    void allowBookingAfterLockExpires() {

        Long seatId = 1L;
        String email = "test@example.com";

        User u = new User();
        Seat s = new Seat();

        s.setStatus(SeatStatus.LOCKED);
        s.setLockedUntil(OffsetDateTime.now().minusMinutes(1));

        Booking old = new Booking();
        old.setStatus(BookingStatus.PENDING);

        when(ur.findByEmail(email))
                .thenReturn(Optional.of(u));

        when(sr.findByIdForUpdate(seatId))
                .thenReturn(Optional.of(s));

        when(br.findBySeatIdAndStatus(seatId, BookingStatus.PENDING))
                .thenReturn(Optional.of(old));

        when(br.save(any(Booking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(br.saveAndFlush(old))
                .thenReturn(old);

        Booking b = bs.bookSeat(seatId, email);

        assertEquals(BookingStatus.EXPIRED, old.getStatus());
        assertEquals(SeatStatus.LOCKED, s.getStatus());
        assertEquals(u, s.getLockedBy());
        assertEquals(BookingStatus.PENDING, b.getStatus());

        verify(br).saveAndFlush(old);
        verify(br).save(any(Booking.class));
        verify(sr).save(s);
    }


}