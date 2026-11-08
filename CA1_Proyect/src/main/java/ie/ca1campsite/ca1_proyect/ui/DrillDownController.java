package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.ds.LinkedList;
import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.Booking;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import ie.ca1campsite.ca1_proyect.model.Plot;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import java.time.LocalDate;

// Controller of the "Drill-down" screen (drill-down-view.fxml).
// It shows campsites > areas > plots > bookings in a tree.
public class DrillDownController extends ScreenController {

    private static final String HELP_TEXT =
            "Choose a campsite, area, plot or booking in the tree to see its details.";

    // Every item of the tree holds one object (Campsite, CampingArea, Plot or Booking)
    @FXML
    private TreeView<Object> treeView;
    @FXML
    private TextArea detailsArea;

    @FXML
    private void initialize() {
        buildTree();

        // Every time the user selects another item, show its details
        treeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldItem, newItem) -> showDetails(newItem));
    }

    // Build the whole tree going through the linked lists of the system
    private void buildTree() {
        TreeItem<Object> root = new TreeItem<>("All campsites");
        root.setExpanded(true);

        Node<Campsite> currentCampsite = AppContext.getManager().getCampsites().getHead();
        while (currentCampsite != null) {
            TreeItem<Object> campsiteItem = new TreeItem<>(currentCampsite.getData());

            Node<CampingArea> currentArea = currentCampsite.getData().getAreas().getHead();
            while (currentArea != null) {
                TreeItem<Object> areaItem = new TreeItem<>(currentArea.getData());

                Node<Plot> currentPlot = currentArea.getData().getPlots().getHead();
                while (currentPlot != null) {
                    TreeItem<Object> plotItem = new TreeItem<>(currentPlot.getData());

                    Node<Booking> currentBooking = currentPlot.getData().getBookings().getHead();
                    while (currentBooking != null) {
                        plotItem.getChildren().add(new TreeItem<>(currentBooking.getData()));
                        currentBooking = currentBooking.getNext();
                    }

                    areaItem.getChildren().add(plotItem);
                    currentPlot = currentPlot.getNext();
                }

                campsiteItem.getChildren().add(areaItem);
                currentArea = currentArea.getNext();
            }

            root.getChildren().add(campsiteItem);
            currentCampsite = currentCampsite.getNext();
        }

        treeView.setRoot(root);
        detailsArea.setText(HELP_TEXT);
    }

    // Show the details of the selected item. The text depends on the type of the object.
    private void showDetails(TreeItem<Object> item) {
        if (item == null) {
            return;
        }

        Object data = item.getValue();

        if (data instanceof Campsite) {
            detailsArea.setText(describeCampsite((Campsite) data));
        } else if (data instanceof CampingArea) {
            detailsArea.setText(describeArea((CampingArea) data));
        } else if (data instanceof Plot) {
            detailsArea.setText(describePlot((Plot) data));
        } else if (data instanceof Booking) {
            detailsArea.setText(describeBooking((Booking) data));
        } else {
            // The root item ("All campsites")
            detailsArea.setText(HELP_TEXT);
        }
    }

    private String describeCampsite(Campsite campsite) {
        return "CAMPSITE\n"
                + "Name: " + campsite.getName() + "\n"
                + "Address: " + campsite.getAddress() + "\n"
                + "Stars: " + campsite.getStars() + "\n"
                + "Contact: " + campsite.getContactInfo() + "\n"
                + "Facilities: " + facilitiesText(campsite.getFacilities()) + "\n"
                + "Camping areas: " + campsite.getAreas().size() + "\n";
    }

    private String describeArea(CampingArea area) {
        return "CAMPING AREA\n"
                + "Identifier: " + area.getIdentifier() + "\n"
                + "Description: " + area.getDescription() + "\n"
                + "Plots: " + area.getPlots().size() + "\n";
    }

    private String describePlot(Plot plot) {
        String text = "PLOT / PITCH\n"
                + "Identifier: " + plot.getId() + "\n"
                + "Type: " + plot.getType() + "\n"
                + "Power: " + yesOrNo(plot.isPowered()) + "\n";

        // Bedrooms and beds only make sense in static dwellings
        if (plot.getType().isStaticDwelling()) {
            text += "Bedrooms: " + plot.getBedrooms() + "\n"
                    + "Beds: " + plot.getBeds() + "\n";
        }

        text += "Size: " + plot.getSizeSqm() + " m2\n"
                + "Cost per night: " + String.format("%.2f", plot.getCostPerNight()) + " EUR\n"
                + "Photo: " + plot.getPhotoUrl() + "\n"
                + "Facilities: " + facilitiesText(plot.getFacilities()) + "\n"
                + "Bookings: " + plot.getBookings().size() + "\n";
        return text;
    }

    private String describeBooking(Booking booking) {
        return "BOOKING #" + booking.getId() + "\n"
                + "Status: " + booking.getStatus(LocalDate.now()) + "\n"
                + "Guest: " + booking.getGuest() + "\n"
                + "Plot: " + booking.getPlot().getId() + "\n"
                + "Check-in: " + booking.getStartDate() + "\n"
                + "Check-out: " + booking.getEndDate() + "\n"
                + "Nights: " + booking.getNights() + "\n"
                + "Number of guests: " + booking.getNumGuests() + "\n"
                + "Total cost: " + String.format("%.2f", booking.getTotalCost()) + " EUR\n";
    }

    // Join the facilities of a list in one text: "pool, wifi, spa"
    private String facilitiesText(LinkedList<String> facilities) {
        if (facilities.isEmpty()) {
            return "none";
        }

        StringBuilder text = new StringBuilder();
        Node<String> current = facilities.getHead();
        while (current != null) {
            text.append(current.getData());
            if (current.getNext() != null) {
                text.append(", ");
            }
            current = current.getNext();
        }
        return text.toString();
    }

    private String yesOrNo(boolean value) {
        if (value) {
            return "Yes";
        }
        return "No";
    }

    // The Refresh button: build the tree again with the current data
    @FXML
    private void onRefresh() {
        buildTree();
    }

    // The Expand all button
    @FXML
    private void onExpandAll() {
        expand(treeView.getRoot());
    }

    // Open an item and, one by one, all the items inside it
    private void expand(TreeItem<Object> item) {
        item.setExpanded(true);
        for (TreeItem<Object> child : item.getChildren()) {
            expand(child);
        }
    }

    @FXML
    private void onBack() {
        goBack();
    }
}