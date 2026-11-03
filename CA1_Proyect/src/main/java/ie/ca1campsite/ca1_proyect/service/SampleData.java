package ie.ca1campsite.ca1_proyect.service;

import ie.ca1campsite.ca1_proyect.model.BookingPreferences;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import ie.ca1campsite.ca1_proyect.model.Plot;
import ie.ca1campsite.ca1_proyect.model.PlotType;
import java.time.LocalDate;

// Fills the system with example data, useful to try the application and for the demo.
public class SampleData {

    // Delete everything and load the example data
    public static void load(CampsiteManager manager) {
        manager.reset();

        // ----- Campsite 1: Lakeside Camping -----
        Campsite lakeside = new Campsite("Lakeside Camping", "Waterford", 4, "info@lakeside.ie");
        lakeside.addFacility("swimming pool");
        lakeside.addFacility("wifi");
        lakeside.addFacility("restaurant");
        lakeside.addFacility("laundry");

        CampingArea lakeView = new CampingArea("Lake View Area 1", "Next to the lake");
        lakeView.addPlot(new Plot("B1", PlotType.BUNGALOW, true, 2, 4, 60, 90.0, "http://photos.example/b1.jpg"));
        lakeView.addPlot(new Plot("B2", PlotType.BUNGALOW, true, 2, 4, 60, 90.0, "http://photos.example/b2.jpg"));
        Plot mobileHome = new Plot("M1", PlotType.STATIC_MOBILE_HOME, true, 2, 4, 45, 70.0, "http://photos.example/m1.jpg");
        mobileHome.addFacility("air conditioning");
        lakeView.addPlot(mobileHome);
        lakeside.addArea(lakeView);

        CampingArea forest = new CampingArea("Forest Area 1", "Quiet area between the trees");
        forest.addPlot(new Plot("T1", PlotType.EMPTY_PITCH, true, 0, 0, 40, 25.0, "http://photos.example/t1.jpg"));
        forest.addPlot(new Plot("T2", PlotType.EMPTY_PITCH, false, 0, 0, 30, 18.0, "http://photos.example/t2.jpg"));
        forest.addPlot(new Plot("T3", PlotType.EMPTY_PITCH, true, 0, 0, 50, 28.0, "http://photos.example/t3.jpg"));
        lakeside.addArea(forest);

        manager.addCampsite(lakeside);

        // ----- Campsite 2: Seaside Park -----
        Campsite seaside = new Campsite("Seaside Park", "Tramore", 3, "info@seaside.ie");
        seaside.addFacility("shop");
        seaside.addFacility("wifi");
        seaside.addFacility("spa");

        CampingArea beach = new CampingArea("Beach Area 1", "A few steps from the beach");
        Plot apartment = new Plot("A1", PlotType.APARTMENT, true, 1, 2, 35, 110.0, "http://photos.example/a1.jpg");
        apartment.addFacility("air conditioning");
        beach.addPlot(apartment);
        beach.addPlot(new Plot("P1", PlotType.EMPTY_PITCH, true, 0, 0, 50, 30.0, "http://photos.example/p1.jpg"));
        seaside.addArea(beach);

        manager.addCampsite(seaside);

        // ----- Guests -----
        manager.addGuest("Ana Garcia", "1990-03-12", "Waterford", "0851111111", "ana@mail.com", "Vegetarian");
        manager.addGuest("Luis Murphy", "1985-07-25", "Cork", "0862222222", "luis@mail.com", "");
        manager.addGuest("Sara Kelly", "1995-11-02", "Dublin", "0873333333", "sara@mail.com", "Travels with a dog");

        // ----- Bookings (all in the future, so they are pending) -----
        BookingPreferences bungalowPrefs = new BookingPreferences(PlotType.BUNGALOW, 2, true, 30, 1, 400);
        manager.makeBooking("Lakeside Camping", "Lake View Area 1", 1,
                LocalDate.of(2027, 7, 10), LocalDate.of(2027, 7, 13), bungalowPrefs);

        BookingPreferences pitchPrefs = new BookingPreferences(PlotType.EMPTY_PITCH, 3, true, 20, 0, 300);
        manager.makeBooking("Lakeside Camping", "Forest Area 1", 2,
                LocalDate.of(2027, 7, 15), LocalDate.of(2027, 7, 20), pitchPrefs);

        BookingPreferences apartmentPrefs = new BookingPreferences(PlotType.APARTMENT, 2, true, 20, 1, 600);
        manager.makeBooking("Seaside Park", "Beach Area 1", 3,
                LocalDate.of(2027, 8, 1), LocalDate.of(2027, 8, 5), apartmentPrefs);
    }
}