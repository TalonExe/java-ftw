module com.talon.testing {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires MaterialFX;  // Add this line
    requires javafx.base;
    requires javafx.graphics;
    
    opens com.talon.testing to javafx.fxml;
    opens com.talon.testing.controllers to javafx.fxml;
    
    // For Gson to access models and possibly the main package
    opens com.talon.testing.models to com.google.gson;
        
    // Export your public API
    exports com.talon.testing;
    exports com.talon.testing.controllers;
    exports com.talon.testing.models;
}