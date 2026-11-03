package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import ie.ca1campsite.ca1_proyect.model.Plot;
import ie.ca1campsite.ca1_proyect.model.PlotType;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

// Controller of the "Add plot" screen (add-plot-view.fxml).
public class AddPlotController extends ScreenController {

    @FXML
    private ComboBox<Campsite> campsiteCombo;
    @FXML
    private ComboBox<CampingArea> areaCombo;
    @FXML
    private TextField idField;
    @FXML
    private ComboBox<PlotType> typeCombo;
    @FXML
    private CheckBox poweredCheck;
    @FXML
    private Spinner<Integer> bedroomsSpinner;
    @FXML
    private Spinner<Integer> bedsSpinner;
    @FXML
    private TextField sizeField;
    @FXML
    private TextField costField;
    @FXML
    private TextField photoField;
    @FXML
    private CheckBox airConditioningCheck;
    @FXML
    private CheckBox heatingCheck;
    @FXML
    private CheckBox barbecueCheck;
    @FXML
    private CheckBox bathroomCheck;

    @FXML
    private void initialize() {
        // Fill the list of campsites
        Node<Campsite> current = AppContext.getManager().getCampsites().getHead();
        while (current != null) {
            campsiteCombo.getItems().add(current.getData());
            current = current.getNext();
        }

        // Fill the list of plot types
        typeCombo.getItems().add(PlotType.STATIC_MOBILE_HOME);
        typeCombo.getItems().add(PlotType.BUNGALOW);
        typeCombo.getItems().add(PlotType.APARTMENT);
        typeCombo.getItems().add(PlotType.EMPTY_PITCH);
        typeCombo.setValue(PlotType.EMPTY_PITCH);

        bedroomsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        bedsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));

        updateTypeFields();
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

    // Called when the user chooses a plot type
    @FXML
    private void onTypeSelected() {
        updateTypeFields();
    }

    // Static dwellings: always powered, with bedrooms and beds.
    // Empty pitches: power is optional and there are no bedrooms or beds.
    private void updateTypeFields() {
        PlotType type = typeCombo.getValue();
        boolean isStatic = type != null && type.isStaticDwelling();

        bedroomsSpinner.setDisable(!isStatic);
        bedsSpinner.setDisable(!isStatic);

        if (isStatic) {
            poweredCheck.setSelected(true);
            poweredCheck.setDisable(true);
        } else {
            poweredCheck.setDisable(false);
        }
    }

    @FXML
    private void onSave() {
        Campsite campsite = campsiteCombo.getValue();
        CampingArea area = areaCombo.getValue();
        String id = idField.getText().trim();
        PlotType type = typeCombo.getValue();
        String photo = photoField.getText().trim();

        if (campsite == null || area == null) {
            AlertHelper.showError("Missing data", "Please choose a campsite and a camping area.");
            return;
        }
        if (id.isEmpty() || photo.isEmpty()) {
            AlertHelper.showError("Missing data", "The plot identifier and the photo URL are required.");
            return;
        }

        // Read the numbers; the user could type something that is not a number
        double size;
        double cost;
        try {
            size = readNumber(sizeField);
            cost = readNumber(costField);
        } catch (NumberFormatException e) {
            AlertHelper.showError("Invalid number", "Size and cost per night must be numbers, for example 40 or 25.5.");
            return;
        }
        if (size <= 0 || cost <= 0) {
            AlertHelper.showError("Invalid number", "Size and cost per night must be greater than 0.");
            return;
        }

        int bedrooms = 0;
        int beds = 0;
        if (type.isStaticDwelling()) {
            bedrooms = bedroomsSpinner.getValue();
            beds = bedsSpinner.getValue();
        }

        Plot plot = new Plot(id, type, poweredCheck.isSelected(), bedrooms, beds, size, cost, photo);
        addIfChecked(plot, airConditioningCheck, "air conditioning");
        addIfChecked(plot, heatingCheck, "heating");
        addIfChecked(plot, barbecueCheck, "barbecue");
        addIfChecked(plot, bathroomCheck, "private bathroom");

        // addPlot returns false if the area already has a plot with this id
        boolean added = area.addPlot(plot);
        if (added) {
            AlertHelper.showInfo("Plot added", "The plot \"" + id + "\" was added to " + area.getIdentifier() + ".");
            setStatus("Plot added: " + id);
            goBack();
        } else {
            AlertHelper.showError("Duplicate identifier", "The area " + area.getIdentifier()
                    + " already has a plot called \"" + id + "\".");
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

    private void addIfChecked(Plot plot, CheckBox box, String facility) {
        if (box.isSelected()) {
            plot.addFacility(facility);
        }
    }
}