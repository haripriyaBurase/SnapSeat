
package com.example.snapseat.booking.pdf;

import com.example.snapseat.booking.Booking;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class TicketPdfService {

    public byte[] generateTicket(Booking booking) throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A6);
            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                content.beginText();
                content.setFont(new PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD
                ), 20);

                content.newLineAtOffset(45, 370);
                content.showText("SNAPSEAT");
                content.endText();

                content.beginText();
                content.setFont(new PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA
                ), 11);

                content.newLineAtOffset(45, 335);
                content.showText("YOUR EVENT TICKET");
                content.endText();

                writeLine(content, "Booking ID: " + booking.getId(), 290);
                writeLine(content, "Event: " + booking.getEvent().getTitle(), 265);
                writeLine(content, "Venue: " + booking.getEvent().getVenue(), 240);

                String eventDate = booking.getEvent()
                        .getEventDate()
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));

                writeLine(content, "Date: " + eventDate, 215);
                writeLine(content, "Seat: " + booking.getSeat().getSeatNumber(), 190);
                writeLine(content, "Tier: " + booking.getSeat().getTier().name(), 165);
                writeLine(content, "Price: Rs. " + booking.getSeat().getPrice(), 140);
                writeLine(content, "Status: " + booking.getStatus().name(), 115);

                writeLine(content, "Thank you for choosing SnapSeat!", 65);
            }

            document.save(output);
            return output.toByteArray();
        }
    }

    private void writeLine(
            PDPageContentStream content,
            String text,
            float y
    ) throws IOException {

        content.beginText();
        content.setFont(new PDType1Font(
                org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA
        ), 10);

        content.newLineAtOffset(45, y);
        content.showText(text);
        content.endText();
    }
}
    