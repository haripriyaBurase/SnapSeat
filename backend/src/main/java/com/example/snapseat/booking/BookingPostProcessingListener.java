
package com.example.snapseat.booking;

import com.example.snapseat.booking.pdf.TicketPdfService;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.annotation.Propagation;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class BookingPostProcessingListener {
    private final BookingRepository bookingRepository;
    private final TicketPdfService ticketPdfService;

    private static final Logger log =
            LoggerFactory.getLogger(BookingPostProcessingListener.class);

    public BookingPostProcessingListener(
            BookingRepository bookingRepository,
            TicketPdfService ticketPdfService
    ) {
        this.bookingRepository = bookingRepository;
        this.ticketPdfService = ticketPdfService;
    }
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public void processBooking(BookingConfirmedEvent event) {


        Long bookingId = event.getBookingId();

        log.info(
                "Background processing started | Booking: {} | Thread: {}",
                bookingId,
                Thread.currentThread().getName()
        );

        try {
            // 1. Fetch booking from database
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() ->
                            new RuntimeException("Booking not found: " + bookingId)
                    );

            // 2. Generate PDF ticket
            byte[] pdf = ticketPdfService.generateTicket(booking);

            // 3. Create tickets directory
            Path ticketsDirectory = Path.of("tickets");
            Files.createDirectories(ticketsDirectory);

            // 4. Save PDF file
            Path ticketPath = ticketsDirectory.resolve(
                    "ticket-" + bookingId + ".pdf"
            );

            Files.write(ticketPath, pdf);

            log.info("Ticket PDF generated successfully: {}", ticketPath);

            // 5. Email placeholder
            log.info("Sending booking confirmation email...");

            log.info("Background processing completed for booking: {}", bookingId);

        } catch (IOException e) {
            log.error("Failed to generate ticket PDF for booking: {}", bookingId, e);
        }

    }
}
