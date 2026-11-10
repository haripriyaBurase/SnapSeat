
package com.example.snapseat.booking;

import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatRepository;
import com.example.snapseat.seat.SeatStatus;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class SeatExpirationService {

    private final SeatRepository sr;
    private final BookingRepository br;

    public SeatExpirationService(
            SeatRepository sr,
            BookingRepository br) {
        this.sr = sr;
        this.br = br;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void releaseExpiredSeats() {

        OffsetDateTime now = OffsetDateTime.now();

        List<Seat> expiredSeats =
                sr.findExpiredLockedSeats(
                        SeatStatus.LOCKED,
                        now
                );

        for (Seat seat : expiredSeats) {

            // Find the pending booking
            Booking booking = br.findBySeatIdAndStatus(
                    seat.getId(),
                    BookingStatus.PENDING
            ).orElse(null);

            // Mark booking as expired
            if (booking != null) {
                booking.setStatus(BookingStatus.EXPIRED);
                br.save(booking);
            }

            // Release the seat
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedBy(null);
            seat.setLockedUntil(null);

            sr.save(seat);

            System.out.println(
                    "Expired reservation released: "
                            + seat.getSeatNumber()
            );
        }
    }
}
