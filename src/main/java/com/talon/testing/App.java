package com.talon.testing;

import com.talon.testing.controllers.FinanceManagerController;
import com.talon.testing.controllers.POApprovalController;
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
import com.talon.testing.controllers.UserController;
import com.talon.testing.models.Sales;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Router router = Router.getInstance();
        router.setPrimaryStage(stage);
        
        router.loadScene("login", "LoginPage.fxml");
        // Load the login scene
        // Finance Manager views
        router.loadScene("PO", "FinanceManager/PO.fxml");
        router.loadScene("PR", "FinanceManager/PR.fxml");
        router.loadScene("Process Payment", "FinanceManager/ProcessPayment.fxml");
        router.loadScene("Finance Report", "FinanceManager/GenerateReport.fxml");
        //router.loadScene("PR", "PR.fxml");
        
        // Purchase Manager views
        router.loadScene("Supplier List", "PurchaseManager/listSupplierView.fxml");
        //router.loadScene("Item List", "PurchaseManager/listItemView.fxml");

       // router.loadScene("PR", "FinanceManager/PR.fxml");
        
        stage.setTitle("Java HRM");
        stage.setWidth(1920);
        stage.setHeight(1080);
        router.switchScene("login");
        stage.show();
    }

    public static void main(String[] args) {
        Router router = Router.getInstance();
        System.out.println(router.getPrimaryStage());
        System.out.println(System.getProperty("user.dir"));
        try {
            System.out.println(Supplier.loadSuppliers());
            System.out.println(Item.loadItems());
            SalesManager hh = new SalesManager();
            System.out.println(hh.getAllowedPermissions());
            
            Item newItem = new Item("IT001", "Laptop", "Business laptop", "1200.00", "50", "10", "2025-04-10");
            boolean added = Item.addItem(newItem);
            System.out.println(added);
            System.out.println(UserController.loadUsers());
            System.out.println(FinanceManagerController.loadPR());
            System.out.println(POApprovalController.loadPO());
            System.out.println(Sales.loadSales());
            
        } catch (IOException e) {
            System.err.print(e);
        }
        launch();
    }

}