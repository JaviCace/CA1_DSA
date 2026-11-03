package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import java.time.LocalDate;

// Controller of the "Pending bookings" screen (pending-bookings-view.fxml).
public class PendingBookingsController extends ScreenController {

    @FXML
    private ComboBox<Campsite> campsiteCombo;
    @FXML
    private TextArea reportArea;

    // Fill the list of campsites
    @FXML
    private void initialize() {
        Node<Campsite> current = AppContext.getManager().getCampsites().getHead();
        while (current != null) {
            campsiteCombo.getItems().add(current.getData());
            current = current.getNext();
        }
    }

    // Show the pending bookings of the chosen campsite (by area and then by plot)
    @FXML
    private void onShowCampsite() {
        Campsite campsite = campsiteCombo.getValue();
        if (campsite == null) {
            AlertHelper.showError("Nothing selected", "Please choose a campsite.");
            return;
        }

        String report = AppContext.getManager()
                .getPendingBookingsReport(campsite.getName(), LocalDate.now());
        if (report == null) {
            AlertHelper.showError("Error", "The campsite could not be found.");
        } else {
            reportArea.setText(report);
        }
    }

    // Show the pending bookings of the whole system (by campsite, area and plot)
    @FXML
    private void onShowAll() {
        if (AppContext.getManager().getCampsites().isEmpty()) {
            reportArea.setText("There are no campsites in the system.");
            return;
        }
        reportArea.setText(AppContext.getManager().getAllPendingBookingsReport(LocalDate.now()));
    }

    @FXML
    private void onBack() {
        goBack();
    }
}