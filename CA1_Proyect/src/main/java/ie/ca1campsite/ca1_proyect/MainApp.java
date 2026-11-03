package ie.ca1campsite.ca1_proyect;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// Starting point of the application: it opens the main window.
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Campsite Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}