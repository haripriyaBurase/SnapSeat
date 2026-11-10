package com.example.snapseat.seat;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventId(Long eventId);

    List<Seat> findByEventIdAndStatus(
            Long eventId,
            SeatStatus status
    );

    boolean existsByEventIdAndSeatNumber(
            Long eventId,
            String seatNumber
    );

    @Query(value = """
    SELECT * FROM seats
    WHERE id = :seatId
    FOR UPDATE
    """, nativeQuery = true)
    Optional<Seat> findByIdForUpdate(@Param("seatId") Long seatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT s FROM Seat s
    WHERE s.status = :status
    AND s.lockedUntil IS NOT NULL
    AND s.lockedUntil <= :now
""")
    List<Seat> findExpiredLockedSeats(
            @Param("status") SeatStatus status,
            @Param("now") OffsetDateTime now
    );

}
