package ie.ca1campsite.ca1_proyect.model;

// The preferences a guest has when asking for a booking.
public class BookingPreferences {
    private PlotType type;       // wanted type of plot (null means "any type")
    private int numGuests;
    private boolean needsPower;
    private double minSize;      // minimum size in square metres
    private int minBedrooms;     // only checked for static dwellings
    private double budget;       // maximum total price for the whole stay (euros)

    public BookingPreferences(PlotType type, int numGuests, boolean needsPower,
                              double minSize, int minBedrooms, double budget) {
        this.type = type;
        this.numGuests = numGuests;
        this.needsPower = needsPower;
        this.minSize = minSize;
        this.minBedrooms = minBedrooms;
        this.budget = budget;
    }

    // Returns true if the plot satisfies all the preferences for a stay of that many nights
    public boolean matches(Plot plot, long nights) {
        // Wrong type of plot
        if (type != null && plot.getType() != type) {
            return false;
        }
        // The guest needs power but the plot has none
        if (needsPower && !plot.isPowered()) {
            return false;
        }
        // Plot too small
        if (plot.getSizeSqm() < minSize) {
            return false;
        }
        // Static dwellings also need enough bedrooms and beds
        if (plot.getType().isStaticDwelling()) {
            if (plot.getBedrooms() < minBedrooms) {
                return false;
            }
            if (plot.getBeds() < numGuests) {
                return false;
            }
        }
        // Too expensive for the budget
        double totalCost = plot.getCostPerNight() * nights;
        if (totalCost > budget) {
            return false;
        }
        return true;
    }

    public PlotType getType() {
        return type;
    }

    public int getNumGuests() {
        return numGuests;
    }

    public boolean isNeedsPower() {
        return needsPower;
    }

    public double getMinSize() {
        return minSize;
    }

    public int getMinBedrooms() {
        return minBedrooms;
    }

    public double getBudget() {
        return budget;
    }
}