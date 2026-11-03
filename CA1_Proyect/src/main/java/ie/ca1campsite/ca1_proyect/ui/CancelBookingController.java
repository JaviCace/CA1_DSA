package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.Booking;
import ie.ca1campsite.ca1_proyect.model.Guest;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import java.time.LocalDate;

// Controller of the "Cancel booking" screen (cancel-booking-view.fxml).
public class CancelBookingController extends ScreenController {

    @FXML
    private ComboBox<Guest> guestCombo;
    @FXML
    private ListView<Booking> bookingList;

    // Fill the list of guests
    @FXML
    private void initialize() {
        Node<Guest> current = AppContext.getManager().getGuests().getHead();
        while (current != null) {
            guestCombo.getItems().add(current.getData());
            current = current.getNext();
        }
    }

    // Called when the user chooses a guest
    @FXML
    private void onGuestSelected() {
        refreshBookings();
    }

    @FXML
    private void onCancelBooking() {
        Booking selected = bookingList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Nothing selected", "Please choose the booking you want to cancel.");
            return;
        }

        boolean sure = AlertHelper.confirm("Cancel booking",
                "Do you really want to cancel booking #" + selected.getId() + "?");
        if (!sure) {
            return;
        }

        boolean cancelled = AppContext.getManager().cancelBooking(selected.getId());
        if (cancelled) {
            AlertHelper.showInfo("Booking cancelled", "Booking #" + selected.getId() + " was cancelled.");
            setStatus("Booking #" + selected.getId() + " cancelled");
        } else {
            AlertHelper.showError("Error", "The booking could not be found.");
        }
        // Show the list again, now without the cancelled booking
        refreshBookings();
    }

    @FXML
    private void onBack() {
        goBack();
    }

    // Show only the PENDING bookings of the chosen guest
    private void refreshBookings() {
        bookingList.getItems().clear();
        Guest guest = guestCombo.getValue();
        if (guest == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        Node<Booking> current = guest.getBookings().getHead();
        while (current != null) {
            if (current.getData().isPending(today)) {
                bookingList.getItems().add(current.getData());
            }
            current = current.getNext();
        }
    }
}