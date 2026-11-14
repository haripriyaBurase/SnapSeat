
package com.example.snapseat.booking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findBySeatIdAndStatus(
            Long seatId,
            BookingStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :bookingId")
    Optional<Booking> findByIdForUpdate(
            @Param("bookingId") Long bookingId
    );

    @Query("SELECT b.seat.id FROM Booking b WHERE b.id = :bookingId")
    Optional<Long> findSeatIdByBookingId(
            @Param("bookingId") Long bookingId
    );

    @Query("""
        SELECT b FROM Booking b
        WHERE b.user.email = :email
        AND b.status = :status
        AND b.seat.lockedUntil > :now
        ORDER BY b.createdAt DESC
    """)
    List<Booking> findActiveBookings(
            @Param("email") String email,
            @Param("status") BookingStatus status,
            @Param("now") OffsetDateTime now
    );
}
