package com.talon.testing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.talon.testing.models.SalesManager;
import com.talon.testing.models.Item;
import com.talon.testing.models.Supplier;
import com.talon.testing.utils.Router;
/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        Router router = Router.getInstance();
        router.setPrimaryStage(stage);
        
        router.loadScene("login", "/primary.fxml");
        
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static public void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        try {
            System.out.println(Supplier.loadSuppliers());
            System.out.println(Item.loadItems());
            SalesManager hh = new SalesManager();
            System.out.println(hh.getAllowedPermissions());
            
            Item newItem = new Item("IT001", "Laptop", "Business laptop", "1200.00", "50", "10", "2025-04-10");
            boolean added = Item.addItem(newItem);
            System.out.println(added);
            
        } catch (IOException e) {
            System.err.print(e);
        }
        launch();
    }

}