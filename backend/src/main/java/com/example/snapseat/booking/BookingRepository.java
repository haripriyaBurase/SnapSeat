package com.example.snapseat.booking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookingRepository  extends JpaRepository<Booking, Long> {
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
}
