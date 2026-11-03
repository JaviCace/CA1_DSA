package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.model.Guest;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.time.LocalDate;

// Controller of the "Add guest" screen (add-guest-view.fxml).
public class AddGuestController extends ScreenController {

    @FXML
    private TextField nameField;
    @FXML
    private DatePicker birthPicker;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextArea notesArea;

    @FXML
    private void onSave() {
        String name = nameField.getText().trim();
        LocalDate birth = birthPicker.getValue();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String notes = notesArea.getText().trim();

        if (name.isEmpty() || birth == null) {
            AlertHelper.showError("Missing data", "Name and date of birth are required.");
            return;
        }
        if (!birth.isBefore(LocalDate.now())) {
            AlertHelper.showError("Invalid date", "The date of birth must be in the past.");
            return;
        }
        // The email is optional, but if it is written it must look like an email
        if (!email.isEmpty() && !email.contains("@")) {
            AlertHelper.showError("Invalid email", "The email must contain an @.");
            return;
        }

        Guest guest = AppContext.getManager().addGuest(name, birth.toString(), address, phone, email, notes);
        AlertHelper.showInfo("Guest added", name + " was added with the guest number " + guest.getId() + ".");
        setStatus("Guest added: " + guest);
        goBack();
    }

    @FXML
    private void onBack() {
        goBack();
    }
}