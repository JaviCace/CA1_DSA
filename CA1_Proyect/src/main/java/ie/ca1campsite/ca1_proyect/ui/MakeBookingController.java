package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.Booking;
import ie.ca1campsite.ca1_proyect.model.BookingPreferences;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import ie.ca1campsite.ca1_proyect.model.Guest;
import ie.ca1campsite.ca1_proyect.model.PlotType;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import java.time.LocalDate;

// Controller of the "Make booking" screen (make-booking-view.fxml).
public class MakeBookingController extends ScreenController {

    // Text of the first option of the type list (it means "any type of plot")
    private static final String ANY_TYPE = "Any type";

    @FXML
    private ComboBox<Campsite> campsiteCombo;
    @FXML
    private ComboBox<CampingArea> areaCombo;
    @FXML
    private ComboBox<Guest> guestCombo;
    @FXML
    private DatePicker startPicker;
    @FXML
    private DatePicker endPicker;
    @FXML
    private Spinner<Integer> guestsSpinner;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private CheckBox powerCheck;
    @FXML
    private TextField minSizeField;
    @FXML
    private Spinner<Integer> bedroomsSpinner;
    @FXML
    private TextField budgetField;

    @FXML
    private void initialize() {
        // Fill the list of campsites
        Node<Campsite> currentCampsite = AppContext.getManager().getCampsites().getHead();
        while (currentCampsite != null) {
            campsiteCombo.getItems().add(currentCampsite.getData());
            currentCampsite = currentCampsite.getNext();
        }

        // Fill the list of guests
        Node<Guest> currentGuest = AppContext.getManager().getGuests().getHead();
        while (currentGuest != null) {
            guestCombo.getItems().add(currentGuest.getData());
            currentGuest = currentGuest.getNext();
        }

        // Fill the list of plot types
        typeCombo.getItems().add(ANY_TYPE);
        typeCombo.getItems().add(PlotType.STATIC_MOBILE_HOME.name());
        typeCombo.getItems().add(PlotType.BUNGALOW.name());
        typeCombo.getItems().add(PlotType.APARTMENT.name());
        typeCombo.getItems().add(PlotType.EMPTY_PITCH.name());
        typeCombo.setValue(ANY_TYPE);

        guestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        bedroomsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
    }

    // Called when the user chooses a campsite: show the areas of that campsite
    @FXML
    private void onCampsiteSelected() {
        areaCombo.getItems().clear();
        Campsite campsite = campsiteCombo.getValue();
        if (campsite == null) {
            return;
        }
        Node<CampingArea> current = campsite.getAreas().getHead();
        while (current != null) {
            areaCombo.getItems().add(current.getData());
            current = current.getNext();
        }
    }

    @FXML
    private void onSave() {
        Campsite campsite = campsiteCombo.getValue();
        CampingArea area = areaCombo.getValue();
        Guest guest = guestCombo.getValue();
        LocalDate start = startPicker.getValue();
        LocalDate end = endPicker.getValue();

        // Check that everything required has been chosen
        if (campsite == null || area == null || guest == null) {
            AlertHelper.showError("Missing data", "Please choose a campsite, a camping area and a guest.");
            return;
        }
        if (start == null || end == null) {
            AlertHelper.showError("Missing data", "Please choose the check-in and check-out dates.");
            return;
        }
        if (start.isBefore(LocalDate.now())) {
            AlertHelper.showError("Invalid dates", "The check-in date cannot be in the past.");
            return;
        }
        if (!end.isAfter(start)) {
            AlertHelper.showError("Invalid dates", "The check-out date must be after the check-in date.");
            return;
        }

        // Read the numbers; the user could type something that is not a number
        double minSize = 0;
        double budget;
        try {
            // The minimum size is optional: if it is empty we use 0
            if (!minSizeField.getText().trim().isEmpty()) {
                minSize = readNumber(minSizeField);
            }
            budget = readNumber(budgetField);
        } catch (NumberFormatException e) {
            AlertHelper.showError("Invalid number", "Minimum size and budget must be numbers, for example 40 or 250.5.");
            return;
        }
        if (minSize < 0 || budget <= 0) {
            AlertHelper.showError("Invalid number", "The budget must be greater than 0 and the size cannot be negative.");
            return;
        }

        // "Any type" means that we do not care about the type (null)
        PlotType type = null;
        if (!typeCombo.getValue().equals(ANY_TYPE)) {
            type = PlotType.valueOf(typeCombo.getValue());
        }

        BookingPreferences prefs = new BookingPreferences(type, guestsSpinner.getValue(),
                powerCheck.isSelected(), minSize, bedroomsSpinner.getValue(), budget);

        // The manager looks for a free plot that matches. It returns null if there is none.
        Booking booking = AppContext.getManager().makeBooking(campsite.getName(),
                area.getIdentifier(), guest.getId(), start, end, prefs);

        if (booking == null) {
            AlertHelper.showError("No plot available", "No plot of " + area.getIdentifier()
                    + " is free on those dates and matches your preferences.\n"
                    + "Try other dates, another area or a higher budget.");
        } else {
            AlertHelper.showInfo("Booking made", "Booking #" + booking.getId() + " created.\n"
                    + "Plot: " + booking.getPlot().getId() + "\n"
                    + "Nights: " + booking.getNights() + "\n"
                    + "Total cost: " + String.format("%.2f", booking.getTotalCost()) + " EUR");
            setStatus("Booking #" + booking.getId() + " made for " + guest.getName());
            goBack();
        }
    }

    @FXML
    private void onBack() {
        goBack();
    }

    // Convert the text of a field to a number (accepts "25,5" and "25.5")
    private double readNumber(TextField field) {
        String text = field.getText().trim().replace(',', '.');
        return Double.parseDouble(text);
    }
}