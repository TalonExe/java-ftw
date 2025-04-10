module com.talon.testing {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens com.talon.testing to javafx.fxml;
    opens com.talon.testing.controllers to javafx.fxml;
    
    exports com.talon.testing.models to com.google.gson;
    exports com.talon.testing.controllers to javafx.fxml;
    exports com.talon.testing;
}
