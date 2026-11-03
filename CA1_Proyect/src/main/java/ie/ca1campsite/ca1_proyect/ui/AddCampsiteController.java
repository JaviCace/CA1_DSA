package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.model.Campsite;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

// Controller of the "Add campsite" screen (add-campsite-view.fxml).
public class AddCampsiteController extends ScreenController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField contactField;
    @FXML
    private Spinner<Integer> starsSpinner;
    @FXML
    private TextField otherFacilityField;

    @FXML
    private CheckBox restaurantCheck;
    @FXML
    private CheckBox poolCheck;
    @FXML
    private CheckBox shopCheck;
    @FXML
    private CheckBox spaCheck;
    @FXML
    private CheckBox gymCheck;
    @FXML
    private CheckBox wifiCheck;
    @FXML
    private CheckBox laundryCheck;
    @FXML
    private CheckBox medicalCheck;

    // JavaFX calls this method automatically when the screen is loaded
    @FXML
    private void initialize() {
        // The stars go from 1 to 5, starting at 3
        starsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3));
    }

    @FXML
    private void onSave() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String contact = contactField.getText().trim();

        // Check that the required fields are not empty
        if (name.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            AlertHelper.showError("Missing data", "Name, address and contact information are required.");
            return;
        }

        Campsite campsite = new Campsite(name, address, starsSpinner.getValue(), contact);

        // Add the facilities that are ticked
        addIfChecked(campsite, restaurantCheck, "restaurant");
        addIfChecked(campsite, poolCheck, "swimming pool");
        addIfChecked(campsite, shopCheck, "shop");
        addIfChecked(campsite, spaCheck, "spa");
        addIfChecked(campsite, gymCheck, "gym");
        addIfChecked(campsite, wifiCheck, "wifi");
        addIfChecked(campsite, laundryCheck, "laundry");
        addIfChecked(campsite, medicalCheck, "medical centre");

        // One extra facility written by the user (optional)
        String other = otherFacilityField.getText().trim();
        if (!other.isEmpty()) {
            campsite.addFacility(other);
        }

        boolean added = AppContext.getManager().addCampsite(campsite);
        if (added) {
            AlertHelper.showInfo("Campsite added", "The campsite \"" + name + "\" was added.");
            setStatus("Campsite added: " + name);
            goBack();   // leave this screen and return to the home screen
        } else {
            AlertHelper.showError("Duplicate name", "There is already a campsite called \"" + name + "\".");
        }
    }

    // The Back button: leave without saving anything
    @FXML
    private void onBack() {
        goBack();
    }

    // Add a facility to the campsite only if its check box is ticked
    private void addIfChecked(Campsite campsite, CheckBox box, String facility) {
        if (box.isSelected()) {
            campsite.addFacility(facility);
        }
    }
}