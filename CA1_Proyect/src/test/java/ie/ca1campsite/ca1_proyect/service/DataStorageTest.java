package ie.ca1campsite.ca1_proyect.service;

import ie.ca1campsite.ca1_proyect.model.Booking;
import ie.ca1campsite.ca1_proyect.model.BookingPreferences;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import ie.ca1campsite.ca1_proyect.model.Plot;
import ie.ca1campsite.ca1_proyect.model.PlotType;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DataStorageTest {

    // Helper: a manager with one campsite, one bungalow, one guest and one booking
    private CampsiteManager createManagerWithBooking() {
        CampsiteManager manager = new CampsiteManager();
        Campsite campsite = new Campsite("Lakeside", "Waterford", 4, "info@lakeside.ie");
        campsite.addFacility("pool");
        CampingArea area = new CampingArea("Lake View 1", "Next to the lake");
        Plot bungalow = new Plot("B1", PlotType.BUNGALOW, true, 2, 4, 60, 90.0, "http://photo");
        bungalow.addFacility("air conditioning");
        area.addPlot(bungalow);
        campsite.addArea(area);
        manager.addCampsite(campsite);
        manager.addGuest("Ana", "1990-01-01", "Waterford", "085", "ana@mail.com", "");

        BookingPreferences prefs = new BookingPreferences(PlotType.BUNGALOW, 2, true, 30, 1, 300);
        manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), prefs);
        return manager;
    }

    // Helper: a temporary file that is deleted when the tests finish
    private File createTempFile() throws IOException {
        File file = File.createTempFile("campsite-test", ".txt");
        file.deleteOnExit();
        return file;
    }

    @Test
    void saveAndLoadKeepsAllTheData() throws IOException {
        CampsiteManager original = createManagerWithBooking();
        File file = createTempFile();

        DataStorage.save(original, file.getAbsolutePath());
        CampsiteManager loaded = new CampsiteManager();
        DataStorage.load(loaded, file.getAbsolutePath());

        // Campsite and its facilities
        Campsite campsite = loaded.findCampsite("Lakeside");
        assertNotNull(campsite);
        assertEquals(4, campsite.getStars());
        assertTrue(campsite.hasFacility("pool"));

        // Plot and its facilities
        Plot plot = campsite.findArea("Lake View 1").findPlot("B1");
        assertNotNull(plot);
        assertEquals(90.0, plot.getCostPerNight());
        assertTrue(plot.hasFacility("air conditioning"));

        // Guest and booking (with its preferences)
        assertEquals("Ana", loaded.findGuest(1).getName());
        Booking booking = loaded.findBooking(1);
        assertNotNull(booking);
        assertEquals(270.0, booking.getTotalCost());
        assertEquals(LocalDate.of(2026, 7, 10), booking.getStartDate());
        assertEquals(PlotType.BUNGALOW, booking.getPreferences().getType());
        assertEquals(300.0, booking.getPreferences().getBudget());

        // The booking is also inside the loaded plot, so the plot is not free
        assertFalse(plot.isAvailable(LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13)));
    }

    @Test
    void idsContinueAfterLoading() throws IOException {
        CampsiteManager original = createManagerWithBooking();   // guest 1 and booking 1
        File file = createTempFile();
        DataStorage.save(original, file.getAbsolutePath());

        CampsiteManager loaded = new CampsiteManager();
        DataStorage.load(loaded, file.getAbsolutePath());

        assertEquals(2, loaded.addGuest("Luis", "1985-05-05", "Cork", "086", "l@mail.com", "").getId());
        BookingPreferences prefs = new BookingPreferences(PlotType.BUNGALOW, 2, true, 30, 1, 300);
        Booking second = loaded.makeBooking("Lakeside", "Lake View 1", 2,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3), prefs);
        assertEquals(2, second.getId());
    }

    @Test
    void textWithSpecialCharactersSurvivesSaveAndLoad() throws IOException {
        CampsiteManager original = new CampsiteManager();
        String notes = "line 1 | with bar\nline 2 with \\ backslash";
        original.addGuest("Ana", "1990-01-01", "Waterford", "085", "ana@mail.com", notes);
        File file = createTempFile();

        DataStorage.save(original, file.getAbsolutePath());
        CampsiteManager loaded = new CampsiteManager();
        DataStorage.load(loaded, file.getAbsolutePath());

        assertEquals(notes, loaded.findGuest(1).getNotes());
    }

    @Test
    void loadingAnInvalidFileThrowsAndLeavesTheSystemEmpty() throws IOException {
        File file = createTempFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("GUEST|abc|this line is wrong\n");
        }
        CampsiteManager manager = createManagerWithBooking();
        String path = file.getAbsolutePath();

        assertThrows(IOException.class, () -> DataStorage.load(manager, path));
        assertEquals(0, manager.getGuests().size());
        assertEquals(0, manager.getCampsites().size());
    }

    @Test
    void loadingAMissingFileDoesNotErasetheData() {
        CampsiteManager manager = createManagerWithBooking();

        assertThrows(IOException.class,
                () -> DataStorage.load(manager, "this-file-does-not-exist.txt"));

        // Nothing was erased
        assertNotNull(manager.findCampsite("Lakeside"));
    }
}