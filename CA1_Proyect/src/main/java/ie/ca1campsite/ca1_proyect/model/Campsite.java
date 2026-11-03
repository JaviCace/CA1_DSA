package ie.ca1campsite.ca1_proyect.model;

import ie.ca1campsite.ca1_proyect.ds.LinkedList;
import ie.ca1campsite.ca1_proyect.ds.Node;

// A campsite: has its own information, facilities and camping areas.
public class Campsite {
    private String name;
    private String address;
    private int stars;
    private String contactInfo;
    private LinkedList<String> facilities;      // for example "pool", "wifi", "spa"
    private LinkedList<CampingArea> areas;

    public Campsite(String name, String address, int stars, String contactInfo) {
        this.name = name;
        this.address = address;
        this.stars = stars;
        this.contactInfo = contactInfo;
        this.facilities = new LinkedList<>();
        this.areas = new LinkedList<>();
    }

    // Add a facility to the campsite (for example "pool")
    public void addFacility(String facility) {
        facilities.add(facility);
    }

    // Check if the campsite has a facility (ignores upper/lower case)
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
    // Check that the campsite has ALL the facilities in the list
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

    // Add a camping area.
    // Returns false (and does not add it) if another area in this campsite
    // already has the same identifier.
    public boolean addArea(CampingArea area) {
        if (findArea(area.getIdentifier()) != null) {
            return false;
        }
        areas.add(area);
        return true;
    }

    // Look for an area by its identifier. Returns null if it is not found.
    public CampingArea findArea(String identifier) {
        Node<CampingArea> current = areas.getHead();
        while (current != null) {
            if (current.getData().getIdentifier().equals(identifier)) {
                return current.getData();
            }
            current = current.getNext();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getStars() {
        return stars;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public LinkedList<String> getFacilities() {
        return facilities;
    }

    public LinkedList<CampingArea> getAreas() {
        return areas;
    }

    @Override
    public String toString() {
        return name + " (" + stars + " stars)";
    }
}