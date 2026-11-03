package ie.ca1campsite.ca1_proyect.service;

import ie.ca1campsite.ca1_proyect.ds.LinkedList;
import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.PlotType;
import ie.ca1campsite.ca1_proyect.model.Booking;
import ie.ca1campsite.ca1_proyect.model.BookingPreferences;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import ie.ca1campsite.ca1_proyect.model.Guest;
import ie.ca1campsite.ca1_proyect.model.Plot;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import ie.ca1campsite.ca1_proyect.model.SmartFindRequest;
import ie.ca1campsite.ca1_proyect.model.SmartFindResult;


// The "brain" of the system: it stores all the campsites and guests
// and offers the operations the user can do.
public class CampsiteManager {
    private LinkedList<Campsite> campsites;
    private LinkedList<Guest> guests;
    private int nextGuestId;     // the id that the next new guest will receive
    private int nextBookingId;   // the id that the next new booking will receive

    public CampsiteManager() {
        campsites = new LinkedList<>();
        guests = new LinkedList<>();
        nextGuestId = 1;
        nextBookingId = 1;
    }

    // ---------- Campsites ----------

    // Add a campsite.
    // Returns false (and does not add it) if there is already a campsite with the same name.
    public boolean addCampsite(Campsite campsite) {
        if (findCampsite(campsite.getName()) != null) {
            return false;
        }
        campsites.add(campsite);
        return true;
    }

    // Look for a campsite by name. Returns null if it is not found.
    public Campsite findCampsite(String name) {
        Node<Campsite> current = campsites.getHead();
        while (current != null) {
            if (current.getData().getName().equals(name)) {
                return current.getData();
            }
            current = current.getNext();
        }
        return null;
    }

    // ---------- Guests ----------

    // Create a new guest. The id is given automatically (1, 2, 3...).
    public Guest addGuest(String name, String dateOfBirth, String address,
                          String phone, String email, String notes) {
        Guest guest = new Guest(nextGuestId, name, dateOfBirth, address, phone, email, notes);
        nextGuestId++;
        guests.add(guest);
        return guest;
    }

    // Look for a guest by id. Returns null if it is not found.
    public Guest findGuest(int id) {
        Node<Guest> current = guests.getHead();
        while (current != null) {
            if (current.getData().getId() == id) {
                return current.getData();
            }
            current = current.getNext();
        }
        return null;
    }

    // ---------- Bookings ----------

    // Make a booking in a camping area. The system looks for the first plot of the
    // area that is free on those dates and matches the preferences.
    // Returns the new booking, or null if something does not exist (campsite, area,
    // guest), the dates are wrong, or no suitable plot is available.
    public Booking makeBooking(String campsiteName, String areaId, int guestId,
                               LocalDate start, LocalDate end, BookingPreferences prefs) {
        Campsite campsite = findCampsite(campsiteName);
        if (campsite == null) {
            return null;
        }
        CampingArea area = campsite.findArea(areaId);
        if (area == null) {
            return null;
        }
        Guest guest = findGuest(guestId);
        if (guest == null) {
            return null;
        }
        if (!end.isAfter(start)) {
            return null;
        }

        long nights = ChronoUnit.DAYS.between(start, end);

        // Go through the plots of the area until one is free and matches
        Node<Plot> current = area.getPlots().getHead();
        while (current != null) {
            Plot plot = current.getData();
            if (plot.isAvailable(start, end) && prefs.matches(plot, nights)) {
                Booking booking = new Booking(nextBookingId, guest, plot, start, end,
                        prefs.getNumGuests());
                booking.setPreferences(prefs);   // remember what the guest asked for
                nextBookingId++;
                plot.addBooking(booking);
                guest.addBooking(booking);
                return booking;
            }
            current = current.getNext();
        }

        // No suitable plot found
        return null;
    }

    // Look for a booking by id. Returns null if it is not found.
    public Booking findBooking(int bookingId) {
        Node<Guest> currentGuest = guests.getHead();
        while (currentGuest != null) {
            Node<Booking> currentBooking = currentGuest.getData().getBookings().getHead();
            while (currentBooking != null) {
                if (currentBooking.getData().getId() == bookingId) {
                    return currentBooking.getData();
                }
                currentBooking = currentBooking.getNext();
            }
            currentGuest = currentGuest.getNext();
        }
        return null;
    }

    // Cancel a booking: it is removed from its plot and from its guest.
    // Returns false if the booking does not exist.
    public boolean cancelBooking(int bookingId) {
        Booking booking = findBooking(bookingId);
        if (booking == null) {
            return false;
        }
        booking.getPlot().removeBooking(booking);
        booking.getGuest().removeBooking(booking);
        return true;
    }
    // ---------- Removing plots ----------

    // Remove a plot from its area. Its pending bookings are moved to another suitable
    // plot of the same campsite (any area). If there is no suitable plot, the booking
    // is cancelled.
    // Returns how many bookings had to be cancelled, or -1 if the campsite, the area
    // or the plot does not exist.
    public int removePlot(String campsiteName, String areaId, String plotId, LocalDate today) {
        Campsite campsite = findCampsite(campsiteName);
        if (campsite == null) {
            return -1;
        }
        CampingArea area = campsite.findArea(areaId);
        if (area == null) {
            return -1;
        }
        Plot plotToRemove = area.findPlot(plotId);
        if (plotToRemove == null) {
            return -1;
        }

        // Remove the plot first, so it cannot be chosen as the new plot
        area.removePlot(plotToRemove);

        int cancelled = 0;
        Node<Booking> current = plotToRemove.getBookings().getHead();
        while (current != null) {
            Booking booking = current.getData();

            // Past bookings are left as they are
            if (booking.isPending(today)) {
                Plot newPlot = findReplacementPlot(campsite, booking);
                if (newPlot != null) {
                    booking.reassignTo(newPlot);
                    newPlot.addBooking(booking);
                } else {
                    // Nowhere to move it: cancel the booking
                    booking.getGuest().removeBooking(booking);
                    cancelled++;
                }
            }
            current = current.getNext();
        }
        return cancelled;
    }

    // Look in all the areas of the campsite for a plot that is free on the dates of
    // the booking and matches what the guest asked for. Returns null if there is none.
    private Plot findReplacementPlot(Campsite campsite, Booking booking) {
        if (booking.getPreferences() == null) {
            return null;
        }

        Node<CampingArea> currentArea = campsite.getAreas().getHead();
        while (currentArea != null) {
            Node<Plot> currentPlot = currentArea.getData().getPlots().getHead();
            while (currentPlot != null) {
                Plot plot = currentPlot.getData();
                if (plot.isAvailable(booking.getStartDate(), booking.getEndDate())
                        && booking.getPreferences().matches(plot, booking.getNights())) {
                    return plot;
                }
                currentPlot = currentPlot.getNext();
            }
            currentArea = currentArea.getNext();
        }
        return null;
    }
    // ---------- Reports (text views) ----------

    // Pending bookings of one campsite, organised by area and then by plot.
    // Returns null if the campsite does not exist.
    public String getPendingBookingsReport(String campsiteName, LocalDate today) {
        Campsite campsite = findCampsite(campsiteName);
        if (campsite == null) {
            return null;
        }
        return buildCampsiteReport(campsite, today);
    }

    // Pending bookings of the whole system, organised by campsite, area and plot.
    public String getAllPendingBookingsReport(LocalDate today) {
        StringBuilder text = new StringBuilder();
        int totalBookings = 0;
        double totalCost = 0;

        Node<Campsite> current = campsites.getHead();
        while (current != null) {
            Campsite campsite = current.getData();
            text.append(buildCampsiteReport(campsite, today)).append("\n");
            totalBookings += countPendingBookings(campsite, today);
            totalCost += sumPendingCost(campsite, today);
            current = current.getNext();
        }

        text.append("=== OVERALL TOTAL ===\n");
        text.append("Total pending bookings: ").append(totalBookings).append("\n");
        text.append("Total cost: ").append(String.format("%.2f", totalCost)).append(" EUR\n");
        return text.toString();
    }

    // The record of one guest: personal data plus pending and past bookings.
    // Returns null if the guest does not exist.
    public String getGuestReport(int guestId, LocalDate today) {
        Guest guest = findGuest(guestId);
        if (guest == null) {
            return null;
        }

        StringBuilder text = new StringBuilder();
        text.append("GUEST #").append(guest.getId()).append(": ").append(guest.getName()).append("\n");
        text.append("Date of birth: ").append(guest.getDateOfBirth()).append("\n");
        text.append("Address: ").append(guest.getAddress()).append("\n");
        text.append("Phone: ").append(guest.getPhone()).append("\n");
        text.append("Email: ").append(guest.getEmail()).append("\n");
        text.append("Notes: ").append(guest.getNotes()).append("\n");

        // First pass: pending bookings
        text.append("\nPending bookings:\n");
        Node<Booking> current = guest.getBookings().getHead();
        while (current != null) {
            if (current.getData().isPending(today)) {
                text.append("  ").append(formatBooking(current.getData())).append("\n");
            }
            current = current.getNext();
        }

        // Second pass: past bookings
        text.append("\nPast bookings:\n");
        current = guest.getBookings().getHead();
        while (current != null) {
            if (!current.getData().isPending(today)) {
                text.append("  ").append(formatBooking(current.getData())).append("\n");
            }
            current = current.getNext();
        }
        return text.toString();
    }

    // Build the text of one campsite: areas, plots with pending bookings, and totals.
    private String buildCampsiteReport(Campsite campsite, LocalDate today) {
        StringBuilder text = new StringBuilder();
        text.append("CAMPSITE: ").append(campsite.getName()).append("\n");

        Node<CampingArea> currentArea = campsite.getAreas().getHead();
        while (currentArea != null) {
            CampingArea area = currentArea.getData();
            text.append("  Area: ").append(area.getIdentifier()).append("\n");

            Node<Plot> currentPlot = area.getPlots().getHead();
            while (currentPlot != null) {
                Plot plot = currentPlot.getData();

                // Only show plots that have pending bookings
                if (countPendingBookings(plot, today) > 0) {
                    text.append("    Plot: ").append(plot.toString()).append("\n");

                    Node<Booking> currentBooking = plot.getBookings().getHead();
                    while (currentBooking != null) {
                        if (currentBooking.getData().isPending(today)) {
                            text.append("      ").append(formatBooking(currentBooking.getData())).append("\n");
                        }
                        currentBooking = currentBooking.getNext();
                    }
                }
                currentPlot = currentPlot.getNext();
            }
            currentArea = currentArea.getNext();
        }

        text.append("  Total pending bookings: ").append(countPendingBookings(campsite, today)).append("\n");
        text.append("  Total cost: ").append(String.format("%.2f", sumPendingCost(campsite, today))).append(" EUR\n");
        return text.toString();
    }

    // One line of text describing a booking
    private String formatBooking(Booking booking) {
        return "Booking #" + booking.getId()
                + " | Plot " + booking.getPlot().getId()
                + " | Guest " + booking.getGuest().getName()
                + " | " + booking.getStartDate() + " to " + booking.getEndDate()
                + " (" + booking.getNights() + " nights)"
                + " | " + String.format("%.2f", booking.getTotalCost()) + " EUR";
    }

    // How many pending bookings one plot has
    private int countPendingBookings(Plot plot, LocalDate today) {
        int count = 0;
        Node<Booking> current = plot.getBookings().getHead();
        while (current != null) {
            if (current.getData().isPending(today)) {
                count++;
            }
            current = current.getNext();
        }
        return count;
    }

    // How many pending bookings a whole campsite has
    private int countPendingBookings(Campsite campsite, LocalDate today) {
        int count = 0;
        Node<CampingArea> currentArea = campsite.getAreas().getHead();
        while (currentArea != null) {
            Node<Plot> currentPlot = currentArea.getData().getPlots().getHead();
            while (currentPlot != null) {
                count += countPendingBookings(currentPlot.getData(), today);
                currentPlot = currentPlot.getNext();
            }
            currentArea = currentArea.getNext();
        }
        return count;
    }

    // Total cost of all the pending bookings of a campsite
    private double sumPendingCost(Campsite campsite, LocalDate today) {
        double total = 0;
        Node<CampingArea> currentArea = campsite.getAreas().getHead();
        while (currentArea != null) {
            Node<Plot> currentPlot = currentArea.getData().getPlots().getHead();
            while (currentPlot != null) {
                Node<Booking> currentBooking = currentPlot.getData().getBookings().getHead();
                while (currentBooking != null) {
                    if (currentBooking.getData().isPending(today)) {
                        total += currentBooking.getData().getTotalCost();
                    }
                    currentBooking = currentBooking.getNext();
                }
                currentPlot = currentPlot.getNext();
            }
            currentArea = currentArea.getNext();
        }
        return total;
    }

    // ---------- Smart find ----------

    // Look for a campsite, area, plot and start date that satisfy the request.
    // It tries the desired date first and then later dates, one day at a time,
    // up to the flexible days. Returns the first match, or null if nothing fits.
    public SmartFindResult smartFind(SmartFindRequest request) {
        for (int shift = 0; shift <= request.getFlexDays(); shift++) {
            LocalDate start = request.getDesiredStart().plusDays(shift);
            LocalDate end = start.plusDays(request.getNights());

            Node<Campsite> currentCampsite = campsites.getHead();
            while (currentCampsite != null) {
                Campsite campsite = currentCampsite.getData();

                // The campsite must have enough stars and all the wanted facilities
                if (campsite.getStars() >= request.getMinStars()
                        && campsite.hasAllFacilities(request.getCampsiteFacilities())) {

                    Node<CampingArea> currentArea = campsite.getAreas().getHead();
                    while (currentArea != null) {
                        CampingArea area = currentArea.getData();

                        Node<Plot> currentPlot = area.getPlots().getHead();
                        while (currentPlot != null) {
                            Plot plot = currentPlot.getData();

                            if (plot.isAvailable(start, end)
                                    && plot.hasAllFacilities(request.getPlotFacilities())
                                    && request.getPreferences().matches(plot, request.getNights())) {
                                double cost = plot.getCostPerNight() * request.getNights();
                                return new SmartFindResult(campsite, area, plot, start, end, cost);
                            }
                            currentPlot = currentPlot.getNext();
                        }
                        currentArea = currentArea.getNext();
                    }
                }
                currentCampsite = currentCampsite.getNext();
            }
        }
        return null;
    }

    // Make the booking proposed by a smart find.
    // Returns null if the guest does not exist.
    public Booking bookFromResult(int guestId, SmartFindResult result, BookingPreferences prefs) {
        Guest guest = findGuest(guestId);
        if (guest == null) {
            return null;
        }
        Booking booking = new Booking(nextBookingId, guest, result.getPlot(),
                result.getStartDate(), result.getEndDate(), prefs.getNumGuests());
        booking.setPreferences(prefs);
        nextBookingId++;
        result.getPlot().addBooking(booking);
        guest.addBooking(booking);
        return booking;
    }
    // ---------- Reset and loading support ----------

    // Remove ALL the data of the system
    public void reset() {
        campsites = new LinkedList<>();
        guests = new LinkedList<>();
        nextGuestId = 1;
        nextBookingId = 1;
    }

    // Add a guest that already has an id (used when loading a file)
    public void addExistingGuest(Guest guest) {
        guests.add(guest);
    }

    public int getNextGuestId() {
        return nextGuestId;
    }

    public void setNextGuestId(int nextGuestId) {
        this.nextGuestId = nextGuestId;
    }

    public int getNextBookingId() {
        return nextBookingId;
    }

    public void setNextBookingId(int nextBookingId) {
        this.nextBookingId = nextBookingId;
    }

    public LinkedList<Campsite> getCampsites() {
        return campsites;
    }

    public LinkedList<Guest> getGuests() {
        return guests;
    }

}