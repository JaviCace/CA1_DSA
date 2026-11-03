package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.MainApp;
import ie.ca1campsite.ca1_proyect.service.DataStorage;
import ie.ca1campsite.ca1_proyect.service.SampleData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;

// Controller of the main window (main-view.fxml).
public class MainController {

    // These fields are connected to the elements of the FXML by their fx:id
    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane contentPane;   // the centre of the window, where screens appear
    @FXML
    private Label statusLabel;       // the text at the bottom of the window

    // ---------- File menu ----------

    @FXML
    private void onSave() {
        FileChooser chooser = createFileChooser("Save data");
        File file = chooser.showSaveDialog(rootPane.getScene().getWindow());

        // The user closed the dialog without choosing a file
        if (file == null) {
            return;
        }

        try {
            DataStorage.save(AppContext.getManager(), file.getAbsolutePath());
            statusLabel.setText("Data saved to " + file.getName());
        } catch (IOException e) {
            AlertHelper.showError("Save error", "The data could not be saved: " + e.getMessage());
        }
    }

    @FXML
    private void onLoad() {
        FileChooser chooser = createFileChooser("Load data");
        File file = chooser.showOpenDialog(rootPane.getScene().getWindow());

        if (file == null) {
            return;
        }

        // Loading replaces all the current data, so we ask first
        boolean sure = AlertHelper.confirm("Load data",
                "Loading a file will replace ALL the current data. Continue?");
        if (!sure) {
            return;
        }

        try {
            DataStorage.load(AppContext.getManager(), file.getAbsolutePath());
            statusLabel.setText("Data loaded from " + file.getName());
        } catch (IOException e) {
            AlertHelper.showError("Load error", "The data could not be loaded: " + e.getMessage());
            statusLabel.setText("Load failed");
        }
    }

    @FXML
    private void onLoadSample() {
        boolean sure = AlertHelper.confirm("Load sample data",
                "This will replace ALL the current data with example data. Continue?");
        if (sure) {
            SampleData.load(AppContext.getManager());
            statusLabel.setText("Sample data loaded");
        }
    }

    @FXML
    private void onReset() {
        boolean sure = AlertHelper.confirm("Reset",
                "This will delete ALL the data of the system. Continue?");
        if (sure) {
            AppContext.getManager().reset();
            statusLabel.setText("All data deleted");
        }
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    // ---------- Campsites menu ----------

    @FXML
    private void onAddCampsite() {
        showScreen("add-campsite-view.fxml");
    }
    @FXML
    private void onAddArea() {
        showScreen("add-area-view.fxml");
    }

    @FXML
    private void onAddPlot() {
        showScreen("add-plot-view.fxml");
    }
    @FXML
    private void onRemovePlot() {
        showScreen("remove-plot-view.fxml");
    }

    @FXML
    private void onAddGuest() {
        showScreen("add-guest-view.fxml");
    }
    // ---------- Bookings menu ----------

    @FXML
    private void onMakeBooking() {
        showScreen("make-booking-view.fxml");
    }

    @FXML
    private void onCancelBooking() {
        showScreen("cancel-booking-view.fxml");
    }
    @FXML
    private void onSmartFind() {
        showScreen("smart-find-view.fxml");
    }
    // ---------- Reports menu ----------

    @FXML
    private void onPendingBookings() {
        showScreen("pending-bookings-view.fxml");
    }

    @FXML
    private void onGuestRecord() {
        showScreen("guest-record-view.fxml");
    }
    @FXML
    private void onDrillDown() {
        showScreen("drill-down-view.fxml");
    }
    // ---------- Methods used by the screens ----------

    // Show the home screen (the welcome message) in the centre of the window
    public void showHome() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Welcome to the Campsite Management System"));
    }

    // Change the text of the status bar
    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    // ---------- Helper methods ----------

    // Load a screen from an FXML file and show it in the centre of the window
    private void showScreen(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlFile));
            Node screen = loader.load();

            // Give the screen a link to this window, so it can go back or change the status
            ScreenController controller = loader.getController();
            controller.setMainController(this);

            contentPane.getChildren().clear();
            contentPane.getChildren().add(screen);
        } catch (IOException e) {
            AlertHelper.showError("Screen error", "The screen could not be opened: " + e.getMessage());
        }
    }

    // Create a file chooser that only shows .txt files
    private FileChooser createFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
        return chooser;
    }
}