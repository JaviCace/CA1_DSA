package ie.ca1campsite.ca1_proyect.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PlotTest {

    // Helper methods to avoid repeating code in every test
    private Plot createPitch() {
        return new Plot("P1", PlotType.EMPTY_PITCH, true, 0, 0, 40, 25.0, "http://photo");
    }

    private Guest createGuest() {
        return new Guest(1, "Ana", "1990-01-01", "Waterford", "085", "ana@mail.com", "");
    }

    @Test
    void staticDwellingAlwaysHasPower() {
        Plot bungalow = new Plot("B1", PlotType.BUNGALOW, false, 2, 4, 60, 90.0, "http://photo");
        assertTrue(bungalow.isPowered());
    }

    @Test
    void plotIsUnavailableWhenDatesOverlap() {
        Plot plot = createPitch();
        Booking booking = new Booking(1, createGuest(), plot,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15), 2);
        plot.addBooking(booking);

        assertFalse(plot.isAvailable(LocalDate.of(2026, 7, 12), LocalDate.of(2026, 7, 18)));
    }

    @Test
    void plotIsAvailableWhenBookingStartsOnDepartureDay() {
        Plot plot = createPitch();
        Booking booking = new Booking(1, createGuest(), plot,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15), 2);
        plot.addBooking(booking);

        assertTrue(plot.isAvailable(LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 20)));
    }

    @Test
    void bookingCostIsNightsTimesPrice() {
        Plot plot = createPitch();   // 25.0 per night
        Booking booking = new Booking(1, createGuest(), plot,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 14), 2);

        assertEquals(100.0, booking.getTotalCost());
    }
}