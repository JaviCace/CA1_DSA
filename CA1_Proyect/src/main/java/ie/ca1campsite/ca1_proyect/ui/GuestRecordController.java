package ie.ca1campsite.ca1_proyect.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.time.LocalDate;

// Controller of the "Guest record" screen (guest-record-view.fxml).
public class GuestRecordController extends ScreenController {

    @FXML
    private TextField idField;
    @FXML
    private TextArea reportArea;

    @FXML
    private void onSearch() {
        // The guest number must be a whole number
        int guestId;
        try {
            guestId = Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException e) {
            AlertHelper.showError("Invalid number", "The guest number must be a whole number, for example 1.");
            return;
        }

        String report = AppContext.getManager().getGuestReport(guestId, LocalDate.now());
        if (report == null) {
            AlertHelper.showError("Guest not found", "There is no guest with the number " + guestId + ".");
            reportArea.clear();
        } else {
            reportArea.setText(report);
        }
    }

    @FXML
    private void onBack() {
        goBack();
    }
}