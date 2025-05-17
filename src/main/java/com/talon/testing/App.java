package com.talon.testing;

import com.talon.testing.controllers.FinanceManagerController;
import com.talon.testing.controllers.InventoryManagerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.talon.testing.models.SalesManager;
import com.talon.testing.models.Item;
import com.talon.testing.models.InventoryManagerModel;
import com.talon.testing.models.Supplier;
import com.talon.testing.utils.Router;
import com.talon.testing.controllers.UserController;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Router router = Router.getInstance();
        router.setPrimaryStage(stage);

        // Load the InventoryManager.fxml scene
        router.loadScene("finance", "/com/talon/testing/InventoryManager.fxml");

        stage.setTitle("Inventory Manager");
        stage.setWidth(1920);
        stage.setHeight(1080);
        router.switchScene("finance");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
