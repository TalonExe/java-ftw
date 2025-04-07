package com.talon.testing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.lang.reflect.Type;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.talon.testing.models.Employee;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    
    private static void loadData() throws IOException {
        
        Map<String, Employee> employeeMap = new HashMap<>();
        // Load employees from JSON
        Gson gson = new Gson();
        try (var inputStream = Employee.class.getResourceAsStream("/data/example.txt")) {
            if (inputStream == null) {
                throw new IOException("File not found: /data/employeeList.json");
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                Type userType = new TypeToken<Map<String, Employee>>() {}.getType();
                employeeMap = gson.fromJson(reader, userType);
            }
        }
        
        System.out.println(employeeMap);
    }

    public static void main(String[] args) {
        try {
            loadData();
        } catch (IOException e) {
            System.err.print(e);
        }
        launch();
    }

}