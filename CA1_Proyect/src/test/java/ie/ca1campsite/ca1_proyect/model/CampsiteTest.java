package ie.ca1campsite.ca1_proyect.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CampsiteTest {

    private Campsite createCampsite() {
        return new Campsite("Lakeside", "Waterford", 4, "info@lakeside.ie");
    }

    @Test
    void addAreaWorksWithNewIdentifier() {
        Campsite campsite = createCampsite();
        assertTrue(campsite.addArea(new CampingArea("Lake View 1", "Next to the lake")));
        assertEquals(1, campsite.getAreas().size());
    }

    @Test
    void duplicateAreaIdentifierIsRejected() {
        Campsite campsite = createCampsite();
        campsite.addArea(new CampingArea("Lake View 1", "Next to the lake"));

        boolean added = campsite.addArea(new CampingArea("Lake View 1", "Another one"));

        assertFalse(added);
        assertEquals(1, campsite.getAreas().size());
    }

    @Test
    void duplicatePlotIdInSameAreaIsRejected() {
        CampingArea area = new CampingArea("Lake View 1", "Next to the lake");
        area.addPlot(new Plot("P1", PlotType.EMPTY_PITCH, true, 0, 0, 40, 25.0, "http://photo"));

        boolean added = area.addPlot(
                new Plot("P1", PlotType.BUNGALOW, true, 2, 4, 60, 90.0, "http://photo"));

        assertFalse(added);
    }

    @Test
    void hasFacilityIgnoresCase() {
        Campsite campsite = createCampsite();
        campsite.addFacility("Pool");

        assertTrue(campsite.hasFacility("pool"));
        assertFalse(campsite.hasFacility("spa"));
    }
}