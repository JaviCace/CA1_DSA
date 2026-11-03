package ie.ca1campsite.ca1_proyect.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// A booking: one guest staying in one plot between two dates.
public class Booking {
    private int id;
    private Guest guest;
    private Plot plot;
    private LocalDate startDate;   // arrival day
    private LocalDate endDate;     // departure day
    private int numGuests;
    private double totalCost;
    private BookingPreferences preferences;   // what the guest asked for

    public Booking(int id, Guest guest, Plot plot, LocalDate startDate,
                   LocalDate endDate, int numGuests) {
        // The departure must be after the arrival
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        this.id = id;
        this.guest = guest;
        this.plot = plot;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numGuests = numGuests;
        this.totalCost = plot.getCostPerNight() * getNights();
    }

    // Number of nights of the stay
    public long getNights() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    // Two stays overlap if each one starts before the other one ends.
    // The departure day is not counted, so a booking that ends on the 15th
    // and another that starts on the 15th do NOT overlap.
    public boolean overlaps(LocalDate otherStart, LocalDate otherEnd) {
        return startDate.isBefore(otherEnd) && otherStart.isBefore(endDate);
    }

    // A booking is pending if it has not finished yet
    public boolean isPending(LocalDate today) {
        return endDate.isAfter(today);
    }

    // Move this booking to another plot and recalculate the total cost
    public void reassignTo(Plot newPlot) {
        this.plot = newPlot;
        this.totalCost = newPlot.getCostPerNight() * getNights();
    }

    public int getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public Plot getPlot() {
        return plot;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getNumGuests() {
        return numGuests;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public BookingPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(BookingPreferences preferences) {
        this.preferences = preferences;
    }

    // Text used to show the booking in lists
    @Override
    public String toString() {
        return "Booking #" + id + " | Plot " + plot.getId() + " | "
                + startDate + " to " + endDate + " | "
                + String.format("%.2f", totalCost) + " EUR";
    }
}