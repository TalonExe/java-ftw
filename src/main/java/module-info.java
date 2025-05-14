module com.talon.testing {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;  // Required for JavaFX property access
    requires com.google.gson; // Required for Gson

    opens com.talon.testing to javafx.fxml;
    opens com.talon.testing.controllers to javafx.fxml;
    opens com.talon.testing.models to javafx.base, com.google.gson; // Allow access to both
    //opens com.talon.testing.models to com.google.gson; // This line is not needed, combined with the above line

    exports com.talon.testing;
    exports com.talon.testing.controllers;
    exports com.talon.testing.models;
}