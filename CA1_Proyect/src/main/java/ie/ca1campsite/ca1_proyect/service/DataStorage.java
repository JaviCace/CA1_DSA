package ie.ca1campsite.ca1_proyect.service;

import ie.ca1campsite.ca1_proyect.ds.LinkedList;
import ie.ca1campsite.ca1_proyect.ds.Node;
import ie.ca1campsite.ca1_proyect.model.Booking;
import ie.ca1campsite.ca1_proyect.model.BookingPreferences;
import ie.ca1campsite.ca1_proyect.model.CampingArea;
import ie.ca1campsite.ca1_proyect.model.Campsite;
import ie.ca1campsite.ca1_proyect.model.Guest;
import ie.ca1campsite.ca1_proyect.model.Plot;
import ie.ca1campsite.ca1_proyect.model.PlotType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

// Saves all the data of the system to a text file and loads it back.
//
// File format: one line per item, with the fields separated by "|".
// The order of the lines matters:
//   CAMPSITE      -> starts a new campsite
//   AREA          -> belongs to the last CAMPSITE
//   PLOT          -> belongs to the last AREA
//   BOOKING       -> belongs to the last PLOT
//   *_FACILITY    -> belongs to the last CAMPSITE or PLOT
public class DataStorage {

    // ---------- Saving ----------

    public static void save(CampsiteManager manager, String filePath) throws IOException {
        // try-with-resources closes the file automatically at the end
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("NEXT_GUEST_ID|" + manager.getNextGuestId());
            writer.newLine();
            writer.write("NEXT_BOOKING_ID|" + manager.getNextBookingId());
            writer.newLine();

            // Guests first, because the bookings refer to them by id
            Node<Guest> currentGuest = manager.getGuests().getHead();
            while (currentGuest != null) {
                Guest guest = currentGuest.getData();
                writer.write("GUEST|" + guest.getId()
                        + "|" + escape(guest.getName())
                        + "|" + escape(guest.getDateOfBirth())
                        + "|" + escape(guest.getAddress())
                        + "|" + escape(guest.getPhone())
                        + "|" + escape(guest.getEmail())
                        + "|" + escape(guest.getNotes()));
                writer.newLine();
                currentGuest = currentGuest.getNext();
            }

            // Campsites, with their areas, plots and bookings inside
            Node<Campsite> currentCampsite = manager.getCampsites().getHead();
            while (currentCampsite != null) {
                Campsite campsite = currentCampsite.getData();
                writer.write("CAMPSITE|" + escape(campsite.getName())
                        + "|" + escape(campsite.getAddress())
                        + "|" + campsite.getStars()
                        + "|" + escape(campsite.getContactInfo()));
                writer.newLine();
                writeFacilities(writer, "CAMPSITE_FACILITY", campsite.getFacilities());

                Node<CampingArea> currentArea = campsite.getAreas().getHead();
                while (currentArea != null) {
                    CampingArea area = currentArea.getData();
                    writer.write("AREA|" + escape(area.getIdentifier())
                            + "|" + escape(area.getDescription()));
                    writer.newLine();

                    Node<Plot> currentPlot = area.getPlots().getHead();
                    while (currentPlot != null) {
                        Plot plot = currentPlot.getData();
                        writer.write("PLOT|" + escape(plot.getId())
                                + "|" + plot.getType().name()
                                + "|" + plot.isPowered()
                                + "|" + plot.getBedrooms()
                                + "|" + plot.getBeds()
                                + "|" + plot.getSizeSqm()
                                + "|" + plot.getCostPerNight()
                                + "|" + escape(plot.getPhotoUrl()));
                        writer.newLine();
                        writeFacilities(writer, "PLOT_FACILITY", plot.getFacilities());

                        Node<Booking> currentBooking = plot.getBookings().getHead();
                        while (currentBooking != null) {
                            writeBooking(writer, currentBooking.getData());
                            currentBooking = currentBooking.getNext();
                        }
                        currentPlot = currentPlot.getNext();
                    }
                    currentArea = currentArea.getNext();
                }
                currentCampsite = currentCampsite.getNext();
            }
        }
    }

    // Write one line for each facility of a list
    private static void writeFacilities(BufferedWriter writer, String lineType,
                                        LinkedList<String> facilities) throws IOException {
        Node<String> current = facilities.getHead();
        while (current != null) {
            writer.write(lineType + "|" + escape(current.getData()));
            writer.newLine();
            current = current.getNext();
        }
    }

    // Write one booking line (the preferences are included in the same line)
    private static void writeBooking(BufferedWriter writer, Booking booking) throws IOException {
        BookingPreferences prefs = booking.getPreferences();
        String prefsText;
        if (prefs == null) {
            prefsText = "NO|NONE|0|false|0|0|0";
        } else {
            String typeText;
            if (prefs.getType() == null) {
                typeText = "NONE";
            } else {
                typeText = prefs.getType().name();
            }
            prefsText = "YES|" + typeText
                    + "|" + prefs.getNumGuests()
                    + "|" + prefs.isNeedsPower()
                    + "|" + prefs.getMinSize()
                    + "|" + prefs.getMinBedrooms()
                    + "|" + prefs.getBudget();
        }

        writer.write("BOOKING|" + booking.getId()
                + "|" + booking.getGuest().getId()
                + "|" + booking.getStartDate()
                + "|" + booking.getEndDate()
                + "|" + booking.getNumGuests()
                + "|" + prefsText);
        writer.newLine();
    }

    // ---------- Loading ----------

    // Load a file and REPLACE all the current data of the manager.
    // If the file is not valid, the manager is left empty and an IOException is thrown.
    public static void load(CampsiteManager manager, String filePath) throws IOException {
        try {
            readFile(manager, filePath);
        } catch (RuntimeException e) {
            // Wrong number, wrong date, unknown line type...
            manager.reset();
            throw new IOException("Invalid data file: " + e.getMessage());
        }
    }

    private static void readFile(CampsiteManager manager, String filePath) throws IOException {
        // If the file does not exist, an exception is thrown here and nothing is erased
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            manager.reset();

            // The item we are currently filling (the lines after it belong to it)
            Campsite currentCampsite = null;
            CampingArea currentArea = null;
            Plot currentPlot = null;

            String line = reader.readLine();
            while (line != null) {
                if (!line.isEmpty()) {
                    LinkedList<String> f = splitLine(line);
                    String lineType = f.get(0);

                    if (lineType.equals("NEXT_GUEST_ID")) {
                        manager.setNextGuestId(Integer.parseInt(f.get(1)));

                    } else if (lineType.equals("NEXT_BOOKING_ID")) {
                        manager.setNextBookingId(Integer.parseInt(f.get(1)));

                    } else if (lineType.equals("GUEST")) {
                        manager.addExistingGuest(new Guest(Integer.parseInt(f.get(1)),
                                f.get(2), f.get(3), f.get(4), f.get(5), f.get(6), f.get(7)));

                    } else if (lineType.equals("CAMPSITE")) {
                        currentCampsite = new Campsite(f.get(1), f.get(2),
                                Integer.parseInt(f.get(3)), f.get(4));
                        manager.addCampsite(currentCampsite);

                    } else if (lineType.equals("CAMPSITE_FACILITY")) {
                        currentCampsite.addFacility(f.get(1));

                    } else if (lineType.equals("AREA")) {
                        currentArea = new CampingArea(f.get(1), f.get(2));
                        currentCampsite.addArea(currentArea);

                    } else if (lineType.equals("PLOT")) {
                        currentPlot = new Plot(f.get(1), PlotType.valueOf(f.get(2)),
                                Boolean.parseBoolean(f.get(3)), Integer.parseInt(f.get(4)),
                                Integer.parseInt(f.get(5)), Double.parseDouble(f.get(6)),
                                Double.parseDouble(f.get(7)), f.get(8));
                        currentArea.addPlot(currentPlot);

                    } else if (lineType.equals("PLOT_FACILITY")) {
                        currentPlot.addFacility(f.get(1));

                    } else if (lineType.equals("BOOKING")) {
                        readBooking(manager, currentPlot, f);

                    } else {
                        throw new IllegalArgumentException("Unknown line type: " + lineType);
                    }
                }
                line = reader.readLine();
            }
        }
    }

    // Rebuild one booking from the fields of its line
    private static void readBooking(CampsiteManager manager, Plot plot, LinkedList<String> f) {
        Guest guest = manager.findGuest(Integer.parseInt(f.get(2)));
        if (guest == null) {
            throw new IllegalArgumentException("Booking of an unknown guest");
        }

        Booking booking = new Booking(Integer.parseInt(f.get(1)), guest, plot,
                LocalDate.parse(f.get(3)), LocalDate.parse(f.get(4)),
                Integer.parseInt(f.get(5)));

        if (f.get(6).equals("YES")) {
            PlotType prefType = null;
            if (!f.get(7).equals("NONE")) {
                prefType = PlotType.valueOf(f.get(7));
            }
            BookingPreferences prefs = new BookingPreferences(prefType,
                    Integer.parseInt(f.get(8)), Boolean.parseBoolean(f.get(9)),
                    Double.parseDouble(f.get(10)), Integer.parseInt(f.get(11)),
                    Double.parseDouble(f.get(12)));
            booking.setPreferences(prefs);
        }

        plot.addBooking(booking);
        guest.addBooking(booking);
    }

    // ---------- Text helpers ----------

    // Make a text safe to put in a line: "\" -> "\\", "|" -> "\|", new line -> "\n"
    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                result.append("\\\\");
            } else if (c == '|') {
                result.append("\\|");
            } else if (c == '\n') {
                result.append("\\n");
            } else if (c != '\r') {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Split a line into its fields (the opposite of escape).
    // We do this by hand because String.split would give us an array.
    private static LinkedList<String> splitLine(String line) {
        LinkedList<String> fields = new LinkedList<>();
        StringBuilder current = new StringBuilder();

        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length()) {
                // An escaped character: \n is a new line, anything else is itself
                char next = line.charAt(i + 1);
                if (next == 'n') {
                    current.append('\n');
                } else {
                    current.append(next);
                }
                i += 2;
            } else if (c == '|') {
                // An unescaped "|" ends the current field
                fields.add(current.toString());
                current = new StringBuilder();
                i++;
            } else {
                current.append(c);
                i++;
            }
        }
        fields.add(current.toString());   // the last field
        return fields;
    }
}