module com.talon.testing {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens com.talon.testing to javafx.fxml;
    exports com.talon.testing;
}
