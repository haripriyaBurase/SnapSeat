
package com.example.snapseat.booking;

import com.example.snapseat.event.Event;
import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatRepository;
import com.example.snapseat.seat.SeatStatus;
import com.example.snapseat.seat.SeatTier;
import com.example.snapseat.user.User;
import com.example.snapseat.user.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@Transactional
class BookingServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("snapseat_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;


    // Create test data

    private User createUser(String email) {
        User user = new User();

        user.setUsername("testuser");
        user.setEmail(email);
        user.setPasswordHash("test-password");

        entityManager.persist(user);
        return user;
    }

    private Event createEvent() {
        Event event = new Event();

        event.setTitle("SnapSeat Integration Test Event");
        event.setDescription("Test event");
        event.setEventDate(OffsetDateTime.now().plusDays(10));
        event.setVenue("Pune Convention Centre");
        event.setTotalSeats(10);
        event.setCreatedAt(LocalDateTime.now());

        entityManager.persist(event);
        return event;
    }

    private Seat createSeat(Event event, SeatStatus status) {
        Seat seat = new Seat();

        seat.setEvent(event);
        seat.setSeatNumber("A1");
        seat.setTier(SeatTier.REGULAR);
        seat.setPrice(new BigDecimal("200.00"));
        seat.setStatus(status);

        entityManager.persist(seat);
        return seat;
    }

    private Booking createPendingBooking(
            User user,
            Event event,
            Seat seat
    ) {
        Booking booking = new Booking();

        booking.setUser(user);
        booking.setEvent(event);
        booking.setSeat(seat);
        booking.setStatus(BookingStatus.PENDING);

        entityManager.persist(booking);
        entityManager.flush();

        return booking;
    }


    // 1. Successfully book an available seat

    @Test
    void bookAvailableSeatSuccessfully() {

        User user = createUser("user1@test.com");
        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.AVAILABLE);

        entityManager.flush();

        Booking booking = bookingService.bookSeat(
                seat.getId(),
                user.getEmail()
        );

        entityManager.flush();
        entityManager.clear();

        Seat updatedSeat = seatRepository.findById(seat.getId())
                .orElseThrow();

        Booking savedBooking = bookingRepository.findById(booking.getId())
                .orElseThrow();

        assertEquals(SeatStatus.LOCKED, updatedSeat.getStatus());
        assertEquals(user.getId(), updatedSeat.getLockedBy().getId());
        assertNotNull(updatedSeat.getLockedUntil());

        assertEquals(BookingStatus.PENDING, savedBooking.getStatus());
        assertEquals(seat.getId(), savedBooking.getSeat().getId());
    }


    // 2. Reject an already booked seat

    @Test
    void rejectAlreadyBookedSeat() {

        User user = createUser("user2@test.com");
        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.BOOKED);

        entityManager.flush();

        assertThrows(
                ResponseStatusException.class,
                () -> bookingService.bookSeat(
                        seat.getId(),
                        user.getEmail()
                )
        );

        assertEquals(0, bookingRepository.count());
    }


    // 3. Reject a seat locked by another user

    @Test
    void rejectSeatLockedByAnotherUser() {

        User owner = createUser("owner@test.com");
        User anotherUser = createUser("another@test.com");

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.LOCKED);

        seat.setLockedBy(owner);
        seat.setLockedUntil(OffsetDateTime.now().plusMinutes(3));

        entityManager.flush();

        assertThrows(
                ResponseStatusException.class,
                () -> bookingService.bookSeat(
                        seat.getId(),
                        anotherUser.getEmail()
                )
        );

        assertEquals(0, bookingRepository.count());
    }


    // 4. Allow booking after an existing lock expires

    @Test
    void allowBookingAfterLockExpires() {

        User oldUser = createUser("olduser@test.com");
        User newUser = createUser("newuser@test.com");

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.LOCKED);

        seat.setLockedBy(oldUser);
        seat.setLockedUntil(OffsetDateTime.now().minusMinutes(1));

        Booking oldBooking = createPendingBooking(
                oldUser,
                event,
                seat
        );

        entityManager.flush();

        Booking newBooking = bookingService.bookSeat(
                seat.getId(),
                newUser.getEmail()
        );

        entityManager.flush();
        entityManager.clear();

        Booking expiredBooking = bookingRepository.findById(oldBooking.getId())
                .orElseThrow();

        Seat updatedSeat = seatRepository.findById(seat.getId())
                .orElseThrow();

        assertEquals(BookingStatus.EXPIRED, expiredBooking.getStatus());
        assertEquals(BookingStatus.PENDING, newBooking.getStatus());

        assertEquals(SeatStatus.LOCKED, updatedSeat.getStatus());
        assertEquals(newUser.getId(), updatedSeat.getLockedBy().getId());
    }


    // 5. Successfully confirm payment

    @Test
    void confirmPaymentSuccessfully() {

        User user = createUser("payer@test.com");
        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.LOCKED);

        seat.setLockedBy(user);
        seat.setLockedUntil(OffsetDateTime.now().plusMinutes(5));

        Booking booking = createPendingBooking(
                user,
                event,
                seat
        );

        entityManager.flush();

        Booking confirmedBooking = bookingService.payForBooking(
                booking.getId(),
                user.getEmail()
        );

        entityManager.flush();
        entityManager.clear();

        Seat updatedSeat = seatRepository.findById(seat.getId())
                .orElseThrow();

        Booking savedBooking = bookingRepository.findById(booking.getId())
                .orElseThrow();

        assertEquals(BookingStatus.CONFIRMED, confirmedBooking.getStatus());
        assertEquals(BookingStatus.CONFIRMED, savedBooking.getStatus());

        assertEquals(SeatStatus.BOOKED, updatedSeat.getStatus());
        assertNull(updatedSeat.getLockedBy());
        assertNull(updatedSeat.getLockedUntil());
    }


    // 6. Reject payment from another user

    @Test
    void rejectPaymentFromAnotherUser() {

        User owner = createUser("bookingowner@test.com");
        User anotherUser = createUser("wronguser@test.com");

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.LOCKED);

        seat.setLockedBy(owner);
        seat.setLockedUntil(OffsetDateTime.now().plusMinutes(5));

        Booking booking = createPendingBooking(
                owner,
                event,
                seat
        );

        entityManager.flush();

        assertThrows(
                ResponseStatusException.class,
                () -> bookingService.payForBooking(
                        booking.getId(),
                        anotherUser.getEmail()
                )
        );

        assertEquals(BookingStatus.PENDING, booking.getStatus());
    }


    // 7. Reject payment after reservation expires

    @Test
    void rejectPaymentAfterReservationExpires() {

        User user = createUser("expired@test.com");
        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.LOCKED);

        seat.setLockedBy(user);
        seat.setLockedUntil(OffsetDateTime.now().minusMinutes(1));

        Booking booking = createPendingBooking(
                user,
                event,
                seat
        );

        entityManager.flush();

        assertThrows(
                ResponseStatusException.class,
                () -> bookingService.payForBooking(
                        booking.getId(),
                        user.getEmail()
                )
        );

        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(SeatStatus.LOCKED, seat.getStatus());
    }


    // 8. Reject duplicate payment

    @Test
    void rejectDuplicatePayment() {

        User user = createUser("duplicate@test.com");
        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.LOCKED);

        seat.setLockedBy(user);
        seat.setLockedUntil(OffsetDateTime.now().plusMinutes(5));

        Booking booking = createPendingBooking(
                user,
                event,
                seat
        );

        entityManager.flush();

        bookingService.payForBooking(
                booking.getId(),
                user.getEmail()
        );

        assertThrows(
                ResponseStatusException.class,
                () -> bookingService.payForBooking(
                        booking.getId(),
                        user.getEmail()
                )
        );
    }
}
