# Campsite Management System

CA Exercise 1 for **Data Structures and Algorithms 1**.

- **Student:** [Your full name]
- **Student number:** [Your student number]
- **Repository:** [Link to your GitHub repository]

A desktop application, written in Java with a JavaFX interface, to manage several campsites. Each campsite has camping areas, each area has plots/pitches, and guests make bookings for those plots.

The main rule of the exercise is that **no Java collections and no arrays are used**. All the data is stored in a linked list written from scratch.

---

## How to run it

**Requirements:** a JDK (17 or newer) and Maven. The project was built with IntelliJ IDEA.

**From IntelliJ:** open the project, let Maven download the dependencies, and run the class `MainApp`.

**From the terminal:**

```
mvn javafx:run
```

**To run all the tests:**

```
mvn test
```

> Tip for trying the application: after starting it, use **File → Load sample data**. It creates two campsites, three guests and three bookings, so you do not have to type everything by hand.

---

## What the application can do

| Feature | Where to find it |
|---|---|
| Add a campsite (name, address, stars, contact, facilities) | Campsites → Add campsite |
| Add a camping area (identifier must be unique in its campsite) | Campsites → Add camping area |
| Add a plot/pitch (type, power, bedrooms, beds, size, facilities, cost, photo URL) | Campsites → Add plot / pitch |
| Remove a plot (its pending bookings are moved to another suitable plot, or cancelled) | Campsites → Remove plot / pitch |
| Add a guest | Guests → Add guest |
| Make a booking (a suitable free plot is assigned automatically) | Bookings → Make booking |
| Cancel a booking | Bookings → Cancel booking |
| Smart find (the system proposes campsite, area, plot and date) | Bookings → Smart find |
| Pending bookings of one campsite, or of the whole system, with totals | Reports → Pending bookings |
| Guest record by guest number, with pending and past bookings | Reports → Guest record |
| Interactive drill-down: campsites → areas → plots → bookings | Reports → Browse campsites |
| Save and load all the data to a text file | File → Save data / Load data |
| Reset (delete all the data) | File → Reset all data |

---

## Project structure

```
src/main/java/ie/ca1campsite/ca1_proyect/
  MainApp.java          Starts the application
  ds/                   Own data structure
    Node.java             One node of the linked list
    LinkedList.java       Generic linked list built from scratch
  model/                Classes that store the data
    Campsite, CampingArea, Plot, PlotType, Guest, Booking,
    BookingPreferences, SmartFindRequest, SmartFindResult
  service/              The logic of the system
    CampsiteManager.java  Stores everything and offers all the operations
    DataStorage.java      Saves and loads the data (text file)
    SampleData.java       Example data for trying the application
  ui/                   JavaFX controllers (one per screen)

src/main/resources/ie/ca1campsite/ca1_proyect/
  *.fxml                Screens, designed with SceneBuilder

src/test/java/          JUnit tests
```

### How the pieces fit together

- The **model** classes only store data. A `Campsite` has a `LinkedList` of `CampingArea`, an area has a `LinkedList` of `Plot`, and a plot has a `LinkedList` of `Booking`. A `Guest` also has a list with all of their bookings.
- `CampsiteManager` is the "brain": it holds the lists of campsites and guests and has the methods for every operation.
- The **screens** never store data. Each controller reads what the user typed, calls `CampsiteManager` and shows the result. All the screens share one manager through `AppContext`.

---

## Data structures

`LinkedList<T>` is a singly linked list (a chain of `Node<T>`). It offers `add`, `get`, `remove`, `size`, `isEmpty`, `clear` and `getHead`. To go through a list, the code starts at `getHead()` and follows `getNext()` until it reaches `null`.

Searching for a campsite, area, plot, guest or booking is a linear search through these lists. Reports and the smart find use nested loops (campsite → area → plot → booking).

Only the lists store the data. The JavaFX components (`ComboBox`, `ListView`, `TreeView`) are filled from these lists just to **display** the data. They use their own internal lists for that, but the data of the system is never kept in them.

---

## Design decisions

The exercise leaves some details open. These are the choices made:

- **Pending booking:** a booking whose check-out date is after today.
- **Dates:** the check-out must be after the check-in. The check-out day counts as free, so a stay that ends on the 15th and another one that starts on the 15th do not overlap.
- **Budget:** the maximum price of the *whole stay* (nights × cost per night).
- **Automatic plot assignment:** the first plot of the chosen area that is free on those dates and matches the preferences (type, power, minimum size, minimum bedrooms, enough beds for the guests in static dwellings, and budget).
- **Static dwellings** (static mobile home, bungalow, apartment) always have power and have bedrooms and beds. Empty pitches have optional power and no bedrooms or beds.
- **Unique names:** campsite names are unique in the system, camping area identifiers are unique inside their campsite, and plot identifiers are unique inside their area.
- **Removing a plot:** its pending bookings are moved to the first suitable free plot of any area of the same campsite. If there is none, the booking is cancelled. Past bookings are not changed.
- **Smart find:** it tries the desired date first and then later dates, one day at a time, up to the flexible days chosen by the user. It checks the minimum stars, the campsite facilities, the plot facilities and the booking preferences, and returns the first match. It only *proposes*: the booking is made when the user confirms it.
- **A booking stores its plot and its guest.** The camping area is found through the plot.

---

## Saving and loading

The data is saved in a plain text file with one line per item. The type of the line is the first word (`GUEST`, `CAMPSITE`, `AREA`, `PLOT`, `BOOKING`...) and the fields are separated by `|`. The order of the lines matters: an `AREA` line belongs to the last `CAMPSITE`, a `PLOT` to the last `AREA`, and so on.

- Characters like `|`, `\` and new lines inside a text are escaped when saving and restored when loading.
- Because arrays are not allowed, the lines are split by hand (not with `String.split`).
- Loading **replaces** all the current data. If the file is not valid, an error is shown and the system is left empty. If the file does not exist, nothing is erased.
- The counters for guest and booking numbers are saved too, so new numbers continue after loading.

---

## Testing

JUnit 5 tests cover the linked list, the model classes, `CampsiteManager` (bookings, cancelling, removing plots, reports, smart find and reset), saving and loading, and the sample data. There are over 40 tests.

The logic was written step by step with its tests. The JavaFX screens are tested by hand, because they only call the logic that is already tested.

---

## Tools

- **Java** with **JavaFX** and **SceneBuilder** for the screens
- **Maven** to manage the project
- **JUnit 5** for the tests
- **Git** and **GitHub** for version control (the history shows the project built step by step)

---

## Known limitations

- Searches go through the lists one by one, which is fine for the size of this application but would be slow with a very large number of items.
- The smart find returns the first match, not the cheapest one.
- The drill-down tree is built when the screen opens. The **Refresh** button builds it again if the data changed.
