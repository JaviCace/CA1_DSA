package ie.ca1campsite.ca1_proyect.model;

import ie.ca1campsite.ca1_proyect.ds.LinkedList;

// A guest of the campsite system.
public class Guest {
    private int id;               // unique number for each guest
    private String name;
    private String dateOfBirth;   // for example "2000-05-14"
    private String address;
    private String phone;
    private String email;
    private String notes;         // any extra information about the guest
    private LinkedList<Booking> bookings;   // all the bookings of this guest

    public Guest(int id, String name, String dateOfBirth, String address,
                 String phone, String email, String notes) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
        this.bookings = new LinkedList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getNotes() {
        return notes;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    // Returns true if the booking was found and removed
    public boolean removeBooking(Booking booking) {
        return bookings.remove(booking);
    }

    public LinkedList<Booking> getBookings() {
        return bookings;
    }

    // Used to show the guest as text, for example in lists
    @Override
    public String toString() {
        return "#" + id + " " + name;
    }
}