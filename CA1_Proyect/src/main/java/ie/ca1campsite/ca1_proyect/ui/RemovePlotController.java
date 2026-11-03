package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.Booking;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import ie.ca1campsite.ca1_proyect.model.Plot;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import java.time.LocalDate;

// Controller of the "Remove plot" screen (remove-plot-view.fxml).
public class RemovePlotController extends ScreenController {

    @FXML
    private ComboBox<Campsite> campsiteCombo;
    @FXML
    private ComboBox<CampingArea> areaCombo;
    @FXML
    private ComboBox<Plot> plotCombo;

    // Fill the list of campsites
    @FXML
    private void initialize() {
        Node<Campsite> current = AppContext.getManager().getCampsites().getHead();
        while (current != null) {
            campsiteCombo.getItems().add(current.getData());
            current = current.getNext();
        }
    }

    // Called when the user chooses a campsite: show its areas
    @FXML
    private void onCampsiteSelected() {
        areaCombo.getItems().clear();
        plotCombo.getItems().clear();
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

    // Called when the user chooses an area: show its plots
    @FXML
    private void onAreaSelected() {
        plotCombo.getItems().clear();
        CampingArea area = areaCombo.getValue();
        if (area == null) {
            return;
        }
        Node<Plot> current = area.getPlots().getHead();
        while (current != null) {
            plotCombo.getItems().add(current.getData());
            current = current.getNext();
        }
    }

    @FXML
    private void onRemove() {
        Campsite campsite = campsiteCombo.getValue();
        CampingArea area = areaCombo.getValue();
        Plot plot = plotCombo.getValue();

        if (campsite == null || area == null || plot == null) {
            AlertHelper.showError("Missing data", "Please choose a campsite, a camping area and a plot.");
            return;
        }

        LocalDate today = LocalDate.now();

        // Count the pending bookings before removing, to report how many were moved
        int pendingBefore = countPending(plot, today);

        boolean sure = AlertHelper.confirm("Remove plot",
                "Plot " + plot.getId() + " will be removed.\n"
                        + "It has " + pendingBefore + " pending booking(s), which will be moved to another "
                        + "suitable plot of " + campsite.getName() + " or cancelled if there is none.\n\nContinue?");
        if (!sure) {
            return;
        }

        int cancelled = AppContext.getManager().removePlot(campsite.getName(),
                area.getIdentifier(), plot.getId(), today);

        if (cancelled == -1) {
            AlertHelper.showError("Error", "The plot could not be found.");
            return;
        }

        int moved = pendingBefore - cancelled;
        AlertHelper.showInfo("Plot removed", "Plot " + plot.getId() + " was removed.\n"
                + "Bookings moved to another plot: " + moved + "\n"
                + "Bookings cancelled (no suitable plot): " + cancelled);
        setStatus("Plot removed: " + plot.getId());

        // Stay on the screen with the plot list updated
        plotCombo.setValue(null);
        onAreaSelected();
    }

    @FXML
    private void onBack() {
        goBack();
    }

    // How many pending bookings a plot has
    private int countPending(Plot plot, LocalDate today) {
        int count = 0;
        Node<Booking> current = plot.getBookings().getHead();
        while (current != null) {
            if (current.getData().isPending(today)) {
                count++;
            }
            current = current.getNext();
        }
        return count;
    }
}