package com.example.snapseat.seat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
