package ie.ca1campsite.ca1_proyect.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void emptyPitchIsNotAStaticDwelling() {
        assertFalse(PlotType.EMPTY_PITCH.isStaticDwelling());
    }

    @Test
    void bungalowIsAStaticDwelling() {
        assertTrue(PlotType.BUNGALOW.isStaticDwelling());
    }

    @Test
    void guestStoresItsData() {
        Guest guest = new Guest(1, "Ana", "1990-01-01", "Waterford",
                "0851234567", "ana@mail.com", "");
        assertEquals(1, guest.getId());
        assertEquals("Ana", guest.getName());
    }
}