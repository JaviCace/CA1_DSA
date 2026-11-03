package ie.ca1campsite.ca1_proyect.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SampleDataTest {

    @Test
    void sampleDataCreatesCampsitesGuestsAndBookings() {
        CampsiteManager manager = new CampsiteManager();

        SampleData.load(manager);

        assertEquals(2, manager.getCampsites().size());
        assertEquals(3, manager.getGuests().size());
        // If the three bookings were created, booking number 3 exists
        assertNotNull(manager.findBooking(3));
    }
}