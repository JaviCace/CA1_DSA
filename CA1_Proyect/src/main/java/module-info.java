module ie.ca1campsite.ca1_proyect {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.junit.jupiter.api;

    opens ie.ca1campsite.ca1_proyect to javafx.fxml;
    exports ie.ca1campsite.ca1_proyect;
    exports ie.ca1campsite.ca1_proyect.ds;
    opens ie.ca1campsite.ca1_proyect.ds;
    exports ie.ca1campsite.ca1_proyect.model;
    opens ie.ca1campsite.ca1_proyect.model;

    exports ie.ca1campsite.ca1_proyect.service;
    opens ie.ca1campsite.ca1_proyect.service;
    exports ie.ca1campsite.ca1_proyect.ui;
    opens ie.ca1campsite.ca1_proyect.ui to javafx.fxml;
}