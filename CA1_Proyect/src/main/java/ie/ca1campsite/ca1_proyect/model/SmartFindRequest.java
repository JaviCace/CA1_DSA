package ie.ca1campsite.ca1_proyect.model;

import ie.ca1campsite.ca1_proyect.ds.LinkedList;
import java.time.LocalDate;

// Everything a guest asks for in a "smart find" search.
public class SmartFindRequest {
    private LocalDate desiredStart;                 // first day the guest would like to arrive
    private int nights;                             // length of the stay
    private int flexDays;                           // how many days later we can start (0 = exact date)
    private int minStars;                           // minimum stars of the campsite
    private LinkedList<String> campsiteFacilities;  // for example "pool", "wifi"
    private LinkedList<String> plotFacilities;      // for example "air conditioning"
    private BookingPreferences preferences;         // type, power, size, bedrooms, budget...

    public SmartFindRequest(LocalDate desiredStart, int nights, int flexDays, int minStars,
                            LinkedList<String> campsiteFacilities,
                            LinkedList<String> plotFacilities,
                            BookingPreferences preferences) {
        this.desiredStart = desiredStart;
        this.nights = nights;
        this.flexDays = flexDays;
        this.minStars = minStars;
        this.campsiteFacilities = campsiteFacilities;
        this.plotFacilities = plotFacilities;
        this.preferences = preferences;
    }

    public LocalDate getDesiredStart() {
        return desiredStart;
    }

    public int getNights() {
        return nights;
    }

    public int getFlexDays() {
        return flexDays;
    }

    public int getMinStars() {
        return minStars;
    }

    public LinkedList<String> getCampsiteFacilities() {
        return campsiteFacilities;
    }

    public LinkedList<String> getPlotFacilities() {
        return plotFacilities;
    }

    public BookingPreferences getPreferences() {
        return preferences;
    }
}