package ie.ca1campsite.ca1_proyect.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    // Helper: a booking from the 10th to the 15th of July 2026
    private Booking createBooking() {
        Plot plot = new Plot("P1", PlotType.EMPTY_PITCH, true, 0, 0, 40, 25.0, "http://photo");
        Guest guest = new Guest(1, "Ana", "1990-01-01", "Waterford", "085", "ana@mail.com", "");
        return new Booking(1, guest, plot,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15), 2);
    }

    @Test
    void statusIsNotStartedYetBeforeTheCheckIn() {
        Booking booking = createBooking();
        LocalDate today = LocalDate.of(2026, 7, 1);

        assertEquals("Not started yet", booking.getStatus(today));
        assertTrue(booking.isPending(today));
    }

    @Test
    void statusIsInProgressDuringTheStay() {
        Booking booking = createBooking();

        assertEquals("In progress", booking.getStatus(LocalDate.of(2026, 7, 10)));   // arrival day
        assertEquals("In progress", booking.getStatus(LocalDate.of(2026, 7, 12)));
        assertTrue(booking.isPending(LocalDate.of(2026, 7, 12)));
    }

    @Test
    void statusIsFinishedFromTheCheckOutDay() {
        Booking booking = createBooking();

        assertEquals("Finished", booking.getStatus(LocalDate.of(2026, 7, 15)));   // departure day
        assertEquals("Finished", booking.getStatus(LocalDate.of(2026, 8, 1)));
        assertFalse(booking.isPending(LocalDate.of(2026, 7, 15)));
    }
}