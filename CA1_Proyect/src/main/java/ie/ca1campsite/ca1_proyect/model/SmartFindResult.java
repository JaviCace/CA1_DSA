package ie.ca1campsite.ca1_proyect.model;

import java.time.LocalDate;

// The proposal found by the smart find: where and when to book.
public class SmartFindResult {
    private Campsite campsite;
    private CampingArea area;
    private Plot plot;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalCost;

    public SmartFindResult(Campsite campsite, CampingArea area, Plot plot,
                           LocalDate startDate, LocalDate endDate, double totalCost) {
        this.campsite = campsite;
        this.area = area;
        this.plot = plot;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
    }

    public Campsite getCampsite() {
        return campsite;
    }

    public CampingArea getArea() {
        return area;
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

    public double getTotalCost() {
        return totalCost;
    }

    // Text to show the proposal to the user
    @Override
    public String toString() {
        return campsite.getName() + " > " + area.getIdentifier() + " > Plot " + plot.getId()
                + " | " + startDate + " to " + endDate
                + " | " + String.format("%.2f", totalCost) + " EUR";
    }
}