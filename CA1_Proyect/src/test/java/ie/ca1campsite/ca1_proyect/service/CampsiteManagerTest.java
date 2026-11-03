package ie.ca1campsite.ca1_proyect.service;

import ie.ca1campsite.ca1_proyect.model.Booking;
import ie.ca1campsite.ca1_proyect.model.BookingPreferences;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import ie.ca1campsite.ca1_proyect.model.Guest;
import ie.ca1campsite.ca1_proyect.model.Plot;
import ie.ca1campsite.ca1_proyect.model.PlotType;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import ie.ca1campsite.ca1_proyect.ds.LinkedList;
import ie.ca1campsite.ca1_proyect.model.SmartFindRequest;
import ie.ca1campsite.ca1_proyect.model.SmartFindResult;
class CampsiteManagerTest {

    // ---------- Helper methods (to avoid repeating code in every test) ----------

    // A manager with one campsite, one area with two bungalows and one guest
    private CampsiteManager createManagerWithData() {
        CampsiteManager manager = new CampsiteManager();
        Campsite campsite = new Campsite("Lakeside", "Waterford", 4, "info@lakeside.ie");
        CampingArea area = new CampingArea("Lake View 1", "Next to the lake");
        area.addPlot(new Plot("B1", PlotType.BUNGALOW, true, 2, 4, 60, 90.0, "http://photo"));
        area.addPlot(new Plot("B2", PlotType.BUNGALOW, true, 2, 4, 60, 90.0, "http://photo"));
        campsite.addArea(area);
        manager.addCampsite(campsite);
        manager.addGuest("Ana", "1990-01-01", "Waterford", "085", "ana@mail.com", "");
        return manager;
    }

    // Preferences for a bungalow with a budget of 300 euros
    private BookingPreferences bungalowPrefs() {
        return new BookingPreferences(PlotType.BUNGALOW, 2, true, 30, 1, 300);
    }

    // ---------- Campsites and guests ----------

    @Test
    void addCampsiteAndFindItByName() {
        CampsiteManager manager = new CampsiteManager();
        manager.addCampsite(new Campsite("Lakeside", "Waterford", 4, "info@lakeside.ie"));

        assertNotNull(manager.findCampsite("Lakeside"));
        assertNull(manager.findCampsite("Unknown"));
    }

    @Test
    void duplicateCampsiteNameIsRejected() {
        CampsiteManager manager = new CampsiteManager();
        manager.addCampsite(new Campsite("Lakeside", "Waterford", 4, "info@lakeside.ie"));

        boolean added = manager.addCampsite(new Campsite("Lakeside", "Cork", 3, "other@mail.ie"));

        assertFalse(added);
        assertEquals(1, manager.getCampsites().size());
    }

    @Test
    void guestsReceiveConsecutiveIds() {
        CampsiteManager manager = new CampsiteManager();
        Guest first = manager.addGuest("Ana", "1990-01-01", "Waterford", "085", "ana@mail.com", "");
        Guest second = manager.addGuest("Luis", "1985-05-05", "Cork", "086", "luis@mail.com", "");

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals("Luis", manager.findGuest(2).getName());
    }

    @Test
    void findGuestReturnsNullWhenIdDoesNotExist() {
        CampsiteManager manager = new CampsiteManager();
        assertNull(manager.findGuest(99));
    }

    // ---------- Bookings ----------

    @Test
    void makeBookingAssignsAMatchingPlot() {
        CampsiteManager manager = createManagerWithData();

        Booking booking = manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), bungalowPrefs());

        assertNotNull(booking);
        assertEquals("B1", booking.getPlot().getId());
        assertEquals(270.0, booking.getTotalCost());
    }

    @Test
    void makeBookingReturnsNullWhenNothingMatches() {
        CampsiteManager manager = createManagerWithData();
        // Budget of only 100 euros: a 3-night bungalow costs 270
        BookingPreferences tooCheap = new BookingPreferences(PlotType.BUNGALOW, 2, true, 30, 1, 100);

        Booking booking = manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), tooCheap);

        assertNull(booking);
    }

    @Test
    void secondBookingOnSameDatesGetsAnotherPlot() {
        CampsiteManager manager = createManagerWithData();
        LocalDate start = LocalDate.of(2026, 7, 10);
        LocalDate end = LocalDate.of(2026, 7, 13);

        Booking first = manager.makeBooking("Lakeside", "Lake View 1", 1, start, end, bungalowPrefs());
        Booking second = manager.makeBooking("Lakeside", "Lake View 1", 1, start, end, bungalowPrefs());
        Booking third = manager.makeBooking("Lakeside", "Lake View 1", 1, start, end, bungalowPrefs());

        assertEquals("B1", first.getPlot().getId());
        assertEquals("B2", second.getPlot().getId());
        assertNull(third);   // both bungalows are taken
    }

    @Test
    void cancelBookingFreesThePlotAndRemovesItFromTheGuest() {
        CampsiteManager manager = createManagerWithData();
        LocalDate start = LocalDate.of(2026, 7, 10);
        LocalDate end = LocalDate.of(2026, 7, 13);
        Booking booking = manager.makeBooking("Lakeside", "Lake View 1", 1, start, end, bungalowPrefs());

        boolean cancelled = manager.cancelBooking(booking.getId());

        assertTrue(cancelled);
        assertTrue(booking.getPlot().isAvailable(start, end));
        assertEquals(0, manager.findGuest(1).getBookings().size());
        assertFalse(manager.cancelBooking(booking.getId()));   // already cancelled
    }
    // ---------- Removing plots ----------

    @Test
    void removePlotMovesPendingBookingToAnotherPlot() {
        CampsiteManager manager = createManagerWithData();
        Booking booking = manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), bungalowPrefs());
        // The booking was assigned to B1

        int cancelled = manager.removePlot("Lakeside", "Lake View 1", "B1", LocalDate.of(2026, 1, 1));

        assertEquals(0, cancelled);
        assertEquals("B2", booking.getPlot().getId());
        CampingArea area = manager.findCampsite("Lakeside").findArea("Lake View 1");
        assertNull(area.findPlot("B1"));
    }

    @Test
    void removePlotCancelsBookingWhenNoOtherPlotIsAvailable() {
        CampsiteManager manager = createManagerWithData();
        LocalDate start = LocalDate.of(2026, 7, 10);
        LocalDate end = LocalDate.of(2026, 7, 13);
        // Both bungalows get booked on the same dates (B1 and B2)
        manager.makeBooking("Lakeside", "Lake View 1", 1, start, end, bungalowPrefs());
        manager.makeBooking("Lakeside", "Lake View 1", 1, start, end, bungalowPrefs());

        int cancelled = manager.removePlot("Lakeside", "Lake View 1", "B1", LocalDate.of(2026, 1, 1));

        assertEquals(1, cancelled);
        assertEquals(1, manager.findGuest(1).getBookings().size());
    }

    @Test
    void removePlotLeavesPastBookingsAlone() {
        CampsiteManager manager = createManagerWithData();
        manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), bungalowPrefs());

        // "Today" is after the stay, so the booking is not pending
        int cancelled = manager.removePlot("Lakeside", "Lake View 1", "B1", LocalDate.of(2026, 12, 1));

        assertEquals(0, cancelled);
        assertEquals(1, manager.findGuest(1).getBookings().size());
    }

    @Test
    void removePlotReturnsMinusOneWhenPlotDoesNotExist() {
        CampsiteManager manager = createManagerWithData();
        assertEquals(-1, manager.removePlot("Lakeside", "Lake View 1", "ZZZ", LocalDate.of(2026, 1, 1)));
    }
    // ---------- Reports ----------

    @Test
    void pendingBookingsReportShowsAreaPlotGuestAndTotals() {
        CampsiteManager manager = createManagerWithData();
        manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), bungalowPrefs());

        String report = manager.getPendingBookingsReport("Lakeside", LocalDate.of(2026, 1, 1));

        assertTrue(report.contains("Area: Lake View 1"));
        assertTrue(report.contains("Plot: B1"));
        assertTrue(report.contains("Guest Ana"));
        assertTrue(report.contains("Total pending bookings: 1"));
    }

    @Test
    void pendingBookingsReportIgnoresPastBookings() {
        CampsiteManager manager = createManagerWithData();
        manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), bungalowPrefs());

        // "Today" is after the stay, so there is nothing pending
        String report = manager.getPendingBookingsReport("Lakeside", LocalDate.of(2026, 12, 1));

        assertTrue(report.contains("Total pending bookings: 0"));
        assertFalse(report.contains("Plot: B1"));
    }

    @Test
    void pendingBookingsReportReturnsNullForUnknownCampsite() {
        CampsiteManager manager = createManagerWithData();
        assertNull(manager.getPendingBookingsReport("Unknown", LocalDate.of(2026, 1, 1)));
    }

    @Test
    void overallReportIncludesAllCampsites() {
        CampsiteManager manager = createManagerWithData();
        manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), bungalowPrefs());

        // A second campsite with one empty pitch
        Campsite seaside = new Campsite("Seaside", "Tramore", 3, "info@seaside.ie");
        CampingArea beach = new CampingArea("Beach 1", "Near the beach");
        beach.addPlot(new Plot("T1", PlotType.EMPTY_PITCH, true, 0, 0, 40, 20.0, "http://photo"));
        seaside.addArea(beach);
        manager.addCampsite(seaside);
        BookingPreferences pitchPrefs =
                new BookingPreferences(PlotType.EMPTY_PITCH, 2, false, 10, 0, 200);
        manager.makeBooking("Seaside", "Beach 1", 1,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 4), pitchPrefs);

        String report = manager.getAllPendingBookingsReport(LocalDate.of(2026, 1, 1));

        assertTrue(report.contains("CAMPSITE: Lakeside"));
        assertTrue(report.contains("CAMPSITE: Seaside"));
        assertTrue(report.contains("OVERALL TOTAL"));
        assertTrue(report.contains("Total pending bookings: 2"));
    }

    @Test
    void guestReportShowsPendingAndPastBookings() {
        CampsiteManager manager = createManagerWithData();
        // Booking #1 is in the past and booking #2 is in the future
        manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), bungalowPrefs());
        manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2027, 8, 10), LocalDate.of(2027, 8, 13), bungalowPrefs());

        String report = manager.getGuestReport(1, LocalDate.of(2026, 12, 1));

        assertTrue(report.contains("GUEST #1: Ana"));
        assertTrue(report.contains("Pending bookings:"));
        assertTrue(report.contains("Past bookings:"));
        assertTrue(report.contains("Booking #1"));
        assertTrue(report.contains("Booking #2"));
    }

    @Test
    void guestReportReturnsNullForUnknownGuest() {
        CampsiteManager manager = createManagerWithData();
        assertNull(manager.getGuestReport(99, LocalDate.of(2026, 1, 1)));
    }
    // ---------- Smart find ----------

    // Helper: a request for 3 nights with no required facilities
    private SmartFindRequest createRequest(LocalDate start, int flexDays, int minStars) {
        return new SmartFindRequest(start, 3, flexDays, minStars,
                new LinkedList<String>(), new LinkedList<String>(), bungalowPrefs());
    }

    @Test
    void smartFindReturnsAMatchOnTheDesiredDate() {
        CampsiteManager manager = createManagerWithData();

        SmartFindResult result = manager.smartFind(createRequest(LocalDate.of(2026, 7, 10), 0, 3));

        assertNotNull(result);
        assertEquals("Lakeside", result.getCampsite().getName());
        assertEquals("B1", result.getPlot().getId());
        assertEquals(LocalDate.of(2026, 7, 10), result.getStartDate());
        assertEquals(270.0, result.getTotalCost());
    }

    @Test
    void smartFindReturnsNullWhenCampsiteHasTooFewStars() {
        CampsiteManager manager = createManagerWithData();   // Lakeside has 4 stars

        SmartFindResult result = manager.smartFind(createRequest(LocalDate.of(2026, 7, 10), 0, 5));

        assertNull(result);
    }

    @Test
    void smartFindChecksCampsiteFacilities() {
        CampsiteManager manager = createManagerWithData();
        SmartFindRequest request = createRequest(LocalDate.of(2026, 7, 10), 0, 3);
        request.getCampsiteFacilities().add("pool");

        // The campsite has no pool yet
        assertNull(manager.smartFind(request));

        manager.findCampsite("Lakeside").addFacility("pool");
        assertNotNull(manager.smartFind(request));
    }

    @Test
    void smartFindChecksPlotFacilities() {
        CampsiteManager manager = createManagerWithData();
        // Only bungalow B2 has air conditioning
        manager.findCampsite("Lakeside").findArea("Lake View 1").findPlot("B2")
                .addFacility("air conditioning");
        SmartFindRequest request = createRequest(LocalDate.of(2026, 7, 10), 0, 3);
        request.getPlotFacilities().add("air conditioning");

        SmartFindResult result = manager.smartFind(request);

        assertNotNull(result);
        assertEquals("B2", result.getPlot().getId());
    }

    @Test
    void smartFindMovesTheStartDateWhenEverythingIsFull() {
        CampsiteManager manager = createManagerWithData();
        LocalDate start = LocalDate.of(2026, 7, 10);
        LocalDate end = LocalDate.of(2026, 7, 13);
        // Both bungalows are booked from the 10th to the 13th
        manager.makeBooking("Lakeside", "Lake View 1", 1, start, end, bungalowPrefs());
        manager.makeBooking("Lakeside", "Lake View 1", 1, start, end, bungalowPrefs());

        // Without flexibility there is nothing
        assertNull(manager.smartFind(createRequest(start, 0, 3)));

        // With 5 flexible days the first free start is the 13th (departure day is free)
        SmartFindResult result = manager.smartFind(createRequest(start, 5, 3));
        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 7, 13), result.getStartDate());
    }

    @Test
    void bookFromResultCreatesTheBooking() {
        CampsiteManager manager = createManagerWithData();
        SmartFindResult result = manager.smartFind(createRequest(LocalDate.of(2026, 7, 10), 0, 3));

        Booking booking = manager.bookFromResult(1, result, bungalowPrefs());

        assertNotNull(booking);
        assertEquals(1, manager.findGuest(1).getBookings().size());
        assertFalse(result.getPlot().isAvailable(result.getStartDate(), result.getEndDate()));
        assertNull(manager.bookFromResult(99, result, bungalowPrefs()));   // unknown guest
    }
    // ---------- Reset ----------

    @Test
    void resetRemovesAllTheData() {
        CampsiteManager manager = createManagerWithData();
        manager.makeBooking("Lakeside", "Lake View 1", 1,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13), bungalowPrefs());

        manager.reset();

        assertEquals(0, manager.getCampsites().size());
        assertEquals(0, manager.getGuests().size());
        // The ids start again from 1
        assertEquals(1, manager.addGuest("Luis", "1985-05-05", "Cork", "086", "l@mail.com", "").getId());
    }
}