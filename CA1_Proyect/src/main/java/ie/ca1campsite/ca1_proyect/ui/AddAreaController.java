package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

// Controller of the "Add camping area" screen (add-area-view.fxml).
public class AddAreaController extends ScreenController {

    @FXML
    private ComboBox<Campsite> campsiteCombo;
    @FXML
    private TextField identifierField;
    @FXML
    private TextArea descriptionArea;

    // Fill the drop-down list with all the campsites of the system
    @FXML
    private void initialize() {
        Node<Campsite> current = AppContext.getManager().getCampsites().getHead();
        while (current != null) {
            campsiteCombo.getItems().add(current.getData());
            current = current.getNext();
        }
    }

    @FXML
    private void onSave() {
        Campsite campsite = campsiteCombo.getValue();
        String identifier = identifierField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (campsite == null) {
            AlertHelper.showError("Missing data", "Please choose a campsite.");
            return;
        }
        if (identifier.isEmpty()) {
            AlertHelper.showError("Missing data", "The area identifier is required.");
            return;
        }

        // addArea returns false if the campsite already has an area with this identifier
        boolean added = campsite.addArea(new CampingArea(identifier, description));
        if (added) {
            AlertHelper.showInfo("Area added", "The area \"" + identifier + "\" was added to "
                    + campsite.getName() + ".");
            setStatus("Area added: " + identifier);
            goBack();
        } else {
            AlertHelper.showError("Duplicate identifier", "The campsite " + campsite.getName()
                    + " already has an area called \"" + identifier + "\".");
        }
    }

    @FXML
    private void onBack() {
        goBack();
    }
}