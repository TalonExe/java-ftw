/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.utils;

import com.talon.testing.controllers.Switchable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author talon
 */
public class Router {
    private static Router instance;
    private Stage primaryStage;
    private Map<String, Scene> scenes;
    private Map<String, Object> controllers;
    private String currentScene;
    private Map<String, String> fxmlPaths = new HashMap<>();
    
    private Router() {
        scenes = new HashMap<>();
        controllers = new HashMap<>();
    }

    public static Router getInstance() {
        if (instance == null) {
            instance = new Router();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void loadScene(String name, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/talon/testing/" + fxmlPath));
        System.out.println(name + " " + fxmlPath);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scenes.put(name, scene);
        controllers.put(name, loader.getController());
        this.fxmlPaths.put(name, "/com/talon/testing/"+fxmlPath);
    }

    public void switchScene(String sceneName) { // This will now RELOAD the FXML
        String fxmlPath = fxmlPaths.get(sceneName);
        if (fxmlPath == null) {
            System.err.println("FXML path not found for scene: " + sceneName);
            return;
        }
        if (primaryStage == null) {
            System.err.println("Primary stage not set in Router.");
            return;
        }

        try {
            System.out.println("Router: Switching to and reloading scene: " + sceneName + " from " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load(); // This re-instantiates controller, calls initialize()
            
            // Store the new controller instance if you need to access it via router.getController()
            // This overwrites any previously stored controller for this sceneName.
            controllers.put(sceneName, loader.getController()); 

            Scene sceneToSet = primaryStage.getScene();
            if (sceneToSet == null) {
                sceneToSet = new Scene(root);
                primaryStage.setScene(sceneToSet);
            } else {
                sceneToSet.setRoot(root);
            }
            // Store the scene if you want to keep a reference, though it's now freshly loaded
            scenes.put(sceneName, sceneToSet); 

            primaryStage.setTitle(sceneName); // Or get title from controller/FXML
            if (!primaryStage.isShowing()) {
                 primaryStage.show();
            }
            this.currentScene = sceneName;

        } catch (IOException e) {
            System.err.println("Error loading scene: " + sceneName + " from path: " + fxmlPath);
            e.printStackTrace();
            // Show an alert to the user
        }
    }

    public Object getController(String name) {
        return controllers.get(name);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public String getCurrentScene() {
        return this.currentScene;
    }
}
