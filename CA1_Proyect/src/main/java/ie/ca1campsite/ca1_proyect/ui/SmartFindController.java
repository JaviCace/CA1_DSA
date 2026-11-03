package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.ds.LinkedList;
import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.Booking;
import ie.ca1campsite.ca1_proyect.model.BookingPreferences;
import ie.ca1campsite.ca1_proyect.model.Guest;
import ie.ca1campsite.ca1_proyect.model.PlotType;
import ie.ca1campsite.ca1_proyect.model.SmartFindRequest;
import ie.ca1campsite.ca1_proyect.model.SmartFindResult;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.time.LocalDate;

// Controller of the "Smart find" screen (smart-find-view.fxml).
public class SmartFindController extends ScreenController {

    // Text of the first option of the type list (it means "any type of plot")
    private static final String ANY_TYPE = "Any type";

    @FXML
    private DatePicker startPicker;
    @FXML
    private Spinner<Integer> nightsSpinner;
    @FXML
    private Spinner<Integer> flexSpinner;
    @FXML
    private Spinner<Integer> starsSpinner;
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

    // Campsite facilities
    @FXML
    private CheckBox poolCheck;
    @FXML
    private CheckBox wifiCheck;
    @FXML
    private CheckBox restaurantCheck;
    @FXML
    private CheckBox spaCheck;
    @FXML
    private CheckBox gymCheck;
    @FXML
    private CheckBox shopCheck;
    @FXML
    private CheckBox laundryCheck;
    @FXML
    private CheckBox medicalCheck;

    // Plot facilities
    @FXML
    private CheckBox airConditioningCheck;
    @FXML
    private CheckBox heatingCheck;
    @FXML
    private CheckBox barbecueCheck;
    @FXML
    private CheckBox bathroomCheck;

    @FXML
    private TextArea resultArea;
    @FXML
    private ComboBox<Guest> guestCombo;
    @FXML
    private Button bookButton;

    // The proposal found by the last search (null if there is none)
    private SmartFindResult lastResult;
    private BookingPreferences lastPrefs;

    @FXML
    private void initialize() {
        nightsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 3));
        flexSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, 0));
        starsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));
        guestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        bedroomsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));

        typeCombo.getItems().add(ANY_TYPE);
        typeCombo.getItems().add(PlotType.STATIC_MOBILE_HOME.name());
        typeCombo.getItems().add(PlotType.BUNGALOW.name());
        typeCombo.getItems().add(PlotType.APARTMENT.name());
        typeCombo.getItems().add(PlotType.EMPTY_PITCH.name());
        typeCombo.setValue(ANY_TYPE);

        // Fill the list of guests
        Node<Guest> current = AppContext.getManager().getGuests().getHead();
        while (current != null) {
            guestCombo.getItems().add(current.getData());
            current = current.getNext();
        }

        // Nothing to book until a search finds something
        bookButton.setDisable(true);
    }

    @FXML
    private void onSearch() {
        // A new search removes the previous proposal
        lastResult = null;
        lastPrefs = null;
        bookButton.setDisable(true);

        LocalDate start = startPicker.getValue();
        if (start == null) {
            AlertHelper.showError("Missing data", "Please choose the desired check-in date.");
            return;
        }
        if (start.isBefore(LocalDate.now())) {
            AlertHelper.showError("Invalid date", "The check-in date cannot be in the past.");
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

        // Build the lists of facilities with the ticked boxes
        // (the names are the same ones used when campsites and plots are created)
        LinkedList<String> campsiteFacilities = new LinkedList<>();
        addIfChecked(campsiteFacilities, poolCheck, "swimming pool");
        addIfChecked(campsiteFacilities, wifiCheck, "wifi");
        addIfChecked(campsiteFacilities, restaurantCheck, "restaurant");
        addIfChecked(campsiteFacilities, spaCheck, "spa");
        addIfChecked(campsiteFacilities, gymCheck, "gym");
        addIfChecked(campsiteFacilities, shopCheck, "shop");
        addIfChecked(campsiteFacilities, laundryCheck, "laundry");
        addIfChecked(campsiteFacilities, medicalCheck, "medical centre");

        LinkedList<String> plotFacilities = new LinkedList<>();
        addIfChecked(plotFacilities, airConditioningCheck, "air conditioning");
        addIfChecked(plotFacilities, heatingCheck, "heating");
        addIfChecked(plotFacilities, barbecueCheck, "barbecue");
        addIfChecked(plotFacilities, bathroomCheck, "private bathroom");

        SmartFindRequest request = new SmartFindRequest(start, nightsSpinner.getValue(),
                flexSpinner.getValue(), starsSpinner.getValue(),
                campsiteFacilities, plotFacilities, prefs);

        SmartFindResult result = AppContext.getManager().smartFind(request);

        if (result == null) {
            resultArea.setText("No campsite matches your search.\n\n"
                    + "Try more flexible days, fewer facilities, fewer stars or a higher budget.");
        } else {
            lastResult = result;
            lastPrefs = prefs;
            resultArea.setText("PROPOSAL\n"
                    + "Campsite: " + result.getCampsite().getName() + " (" + result.getCampsite().getStars() + " stars)\n"
                    + "Area: " + result.getArea().getIdentifier() + "\n"
                    + "Plot: " + result.getPlot() + "\n"
                    + "Check-in: " + result.getStartDate() + "\n"
                    + "Check-out: " + result.getEndDate() + "\n"
                    + "Total cost: " + String.format("%.2f", result.getTotalCost()) + " EUR\n\n"
                    + "Choose a guest below and press \"Book this proposal\" to confirm it.");
            bookButton.setDisable(false);
        }
    }

    @FXML
    private void onBook() {
        if (lastResult == null) {
            return;
        }
        Guest guest = guestCombo.getValue();
        if (guest == null) {
            AlertHelper.showError("Missing data", "Please choose the guest who makes the booking.");
            return;
        }

        Booking booking = AppContext.getManager().bookFromResult(guest.getId(), lastResult, lastPrefs);
        if (booking == null) {
            AlertHelper.showError("Error", "The booking could not be made.");
        } else {
            AlertHelper.showInfo("Booking made", "Booking #" + booking.getId() + " created for "
                    + guest.getName() + ".\n" + lastResult);
            setStatus("Booking #" + booking.getId() + " made with smart find");
            goBack();
        }
    }

    @FXML
    private void onBack() {
        goBack();
    }

    // Add a facility to the list only if its check box is ticked
    private void addIfChecked(LinkedList<String> list, CheckBox box, String facility) {
        if (box.isSelected()) {
            list.add(facility);
        }
    }

    // Convert the text of a field to a number (accepts "25,5" and "25.5")
    private double readNumber(TextField field) {
        String text = field.getText().trim().replace(',', '.');
        return Double.parseDouble(text);
    }
}