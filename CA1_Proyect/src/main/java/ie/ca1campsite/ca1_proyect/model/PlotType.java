package ie.ca1campsite.ca1_proyect.model;

// The different types of plot/pitch a camping area can have.
public enum PlotType {
    STATIC_MOBILE_HOME,
    BUNGALOW,
    APARTMENT,
    EMPTY_PITCH;

    // Returns true for static dwellings (everything except an empty pitch).
    // Static dwellings have bedrooms, beds and always have power.
    public boolean isStaticDwelling() {
        return this != EMPTY_PITCH;
    }
}