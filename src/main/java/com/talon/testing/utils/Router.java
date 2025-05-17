package com.talon.testing.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Router {
    private static Router instance;
    private Stage primaryStage;
    private Map<String, Scene> scenes;
    private Map<String, Object> controllers;
    private String currentScene;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath)); // Use full path
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scenes.put(name, scene);
        controllers.put(name, loader.getController());
    }

    public void switchScene(String sceneName) {
        Scene scene = scenes.get(sceneName);
        if (scene != null && primaryStage != null) {
            primaryStage.setScene(scene);
        } else {
            System.err.println("Scene not found: " + sceneName);
        }
        this.currentScene = sceneName;
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
