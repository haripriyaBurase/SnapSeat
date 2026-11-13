package com.example.snapseat.booking;

import com.example.snapseat.seat.Seat;
import com.example.snapseat.seat.SeatRepository;
import com.example.snapseat.seat.SeatStatus;
import com.example.snapseat.user.User;
import com.example.snapseat.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.example.snapseat.seat.SeatUpdate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class BookingService {
    private final BookingRepository br;
    private final SeatRepository sr;
    private final UserRepository ur;
    private final SimpMessagingTemplate messagingTemplate;


    public BookingService(
            BookingRepository br,
            SeatRepository sr,
            UserRepository ur,
            SimpMessagingTemplate messagingTemplate) {

        this.br = br;
        this.sr = sr;
        this.ur = ur;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Booking bookSeat(Long seatId, String email){
        User u = ur.findByEmail(email)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Seat s = sr.findByIdForUpdate(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        OffsetDateTime now = OffsetDateTime.now();
        if (s.getStatus() == SeatStatus.BOOKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat is already booked");
        }
        if (s.getStatus() == SeatStatus.LOCKED &&
                s.getLockedUntil() != null &&
                s.getLockedUntil().isAfter(now)) {

            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat is currently locked");

        }
        if (s.getStatus() == SeatStatus.LOCKED &&
                (s.getLockedUntil() == null ||
                        !s.getLockedUntil().isAfter(now))) {

            Booking b = br.findBySeatIdAndStatus(
                    seatId, BookingStatus.PENDING
            ).orElse(null);
            if(b!=null){
                b.setStatus(BookingStatus.EXPIRED);
                br.saveAndFlush(b);
            }
        }
            s.setStatus(SeatStatus.LOCKED);
            s.setLockedBy(u);
            s.setLockedUntil(now.plusMinutes(5));

            sr.save(s);

        Booking booking = new Booking();
        booking.setUser(u);
        booking.setEvent(s.getEvent());
        booking.setSeat(s);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = br.save(booking);

        SeatUpdate update = new SeatUpdate(
                s.getId(),
                s.getSeatNumber(),
                s.getStatus(),
                s.getEvent().getId()
        );

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend(
                                "/topic/events/" + s.getEvent().getId() + "/seats",
                                update
                        );
                    }
                }
        );

        return savedBooking;
    }
}
