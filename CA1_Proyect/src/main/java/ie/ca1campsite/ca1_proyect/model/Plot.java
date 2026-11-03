package ie.ca1campsite.ca1_proyect.model;

import ie.ca1campsite.ca1_proyect.ds.LinkedList;
import ie.ca1campsite.ca1_proyect.ds.Node;
import java.time.LocalDate;
import ie.ca1campsite.ca1_proyect.model.SmartFindRequest;
import ie.ca1campsite.ca1_proyect.model.SmartFindResult;
// A plot/pitch inside a camping area.
// It can be a static dwelling (bungalow, mobile home...) or an empty pitch.
public class Plot {
    private String id;
    private PlotType type;
    private boolean powered;
    private int bedrooms;
    private int beds;
    private double sizeSqm;
    private double costPerNight;
    private String photoUrl;
    private LinkedList<String> facilities;   // for example "air conditioning"
    private LinkedList<Booking> bookings;    // bookings made for this plot

    public Plot(String id, PlotType type, boolean powered, int bedrooms, int beds,
                double sizeSqm, double costPerNight, String photoUrl) {
        this.id = id;
        this.type = type;
        this.sizeSqm = sizeSqm;
        this.costPerNight = costPerNight;
        this.photoUrl = photoUrl;
        this.facilities = new LinkedList<>();
        this.bookings = new LinkedList<>();

        if (type.isStaticDwelling()) {
            // Static dwellings always have power and have bedrooms and beds
            this.powered = true;
            this.bedrooms = bedrooms;
            this.beds = beds;
        } else {
            // Empty pitches: power is optional and there are no bedrooms or beds
            this.powered = powered;
            this.bedrooms = 0;
            this.beds = 0;
        }
    }

    // Add a facility to this plot (for example "air conditioning")
    public void addFacility(String facility) {
        facilities.add(facility);
    }

    // Check if this plot has a facility (ignores upper/lower case)
    public boolean hasFacility(String facility) {
        Node<String> current = facilities.getHead();
        while (current != null) {
            if (current.getData().equalsIgnoreCase(facility)) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }
    // Check that the plot has ALL the facilities in the list
    public boolean hasAllFacilities(LinkedList<String> required) {
        Node<String> current = required.getHead();
        while (current != null) {
            if (!hasFacility(current.getData())) {
                return false;
            }
            current = current.getNext();
        }
        return true;
    }

    // The plot is available if no existing booking overlaps with the dates
    public boolean isAvailable(LocalDate start, LocalDate end) {
        Node<Booking> current = bookings.getHead();
        while (current != null) {
            if (current.getData().overlaps(start, end)) {
                return false;
            }
            current = current.getNext();
        }
        return true;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    // Returns true if the booking was found and removed
    public boolean removeBooking(Booking booking) {
        return bookings.remove(booking);
    }

    public String getId() {
        return id;
    }

    public PlotType getType() {
        return type;
    }

    public boolean isPowered() {
        return powered;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public int getBeds() {
        return beds;
    }

    public double getSizeSqm() {
        return sizeSqm;
    }

    public double getCostPerNight() {
        return costPerNight;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public LinkedList<String> getFacilities() {
        return facilities;
    }

    public LinkedList<Booking> getBookings() {
        return bookings;
    }

    @Override
    public String toString() {
        return id + " (" + type + ")";
    }
}