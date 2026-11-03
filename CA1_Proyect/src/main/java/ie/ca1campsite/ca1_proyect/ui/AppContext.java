package ie.ca1campsite.ca1_proyect.ui;

import ie.ca1campsite.ca1_proyect.service.CampsiteManager;

// Holds the ONE manager shared by all the screens of the application.
// This way every screen works with the same data.
public class AppContext {
    private static CampsiteManager manager = new CampsiteManager();

    public static CampsiteManager getManager() {
        return manager;
    }
}