package ie.ca1campsite.ca1_proyect.ui;

// Base class of all the screens that are shown inside the main window.
// It lets a screen talk to the main window (for example, to go back to the home screen).
public abstract class ScreenController {
    private MainController mainController;

    // Called by the main window when it loads the screen
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Go back to the home screen
    protected void goBack() {
        mainController.showHome();
    }

    // Show a message in the status bar at the bottom of the window
    protected void setStatus(String message) {
        mainController.setStatus(message);
    }
}