package ie.ca1campsite.ca1_proyect.model;

import ie.ca1campsite.ca1_proyect.ds.LinkedList;
import ie.ca1campsite.ca1_proyect.ds.Node;

// A camping area: a group of plots inside a campsite.
public class CampingArea {
    private String identifier;   // for example "Lake View Area 1"
    private String description;
    private LinkedList<Plot> plots;

    public CampingArea(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
        this.plots = new LinkedList<>();
    }

    // Add a plot to this area.
    // Returns false (and does not add it) if the area already has a plot with the same id.
    public boolean addPlot(Plot plot) {
        if (findPlot(plot.getId()) != null) {
            return false;
        }
        plots.add(plot);
        return true;
    }

    // Look for a plot by its id. Returns null if it is not found.
    public Plot findPlot(String plotId) {
        Node<Plot> current = plots.getHead();
        while (current != null) {
            if (current.getData().getId().equals(plotId)) {
                return current.getData();
            }
            current = current.getNext();
        }
        return null;
    }

    // Returns true if the plot was found and removed
    public boolean removePlot(Plot plot) {
        return plots.remove(plot);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public LinkedList<Plot> getPlots() {
        return plots;
    }

    @Override
    public String toString() {
        return identifier;
    }
}