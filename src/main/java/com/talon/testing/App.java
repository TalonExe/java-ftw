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
import java.time.LocalDate;

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
        
        //Sales Manager views
        router.loadScene("Create PR", "SalesManager/CreatePR.fxml");
        router.loadScene("Supplier Entry", "SalesManager/SupplierEntry.fxml");
        router.loadScene("View PO", "SalesManager/PO.fxml");
        router.loadScene("View PR", "SalesManager/PR.fxml");
        router.loadScene("Item Entry", "SalesManager/ItemEntry.fxml");
        router.loadScene("Sales Entry", "SalesManager/DailySales.fxml");
        
        // Finance Manager views
        router.loadScene("PO", "FinanceManager/PO.fxml");
        router.loadScene("PR", "FinanceManager/PR.fxml");
        router.loadScene("Process Payment", "FinanceManager/ProcessPayment.fxml");
        router.loadScene("Finance Report", "FinanceManager/GenerateReport.fxml");
        
        // Purchase Manager views
        router.loadScene("Supplier List", "PurchaseManager/listSupplierView.fxml");
        router.loadScene("Item List", "PurchaseManager/listItemView.fxml");
        router.loadScene("View PR PM", "PurchaseManager/PR.fxml");
        router.loadScene("Create PO", "PurchaseManager/CreatePO.fxml");

        // Inventory Manager views
        router.loadScene("Stock Management", "InventoryManager/StockManagement.fxml");
        router.loadScene("Generate Report", "InventoryManager/GenerateInventoryReport.fxml");
        router.loadScene("View PR IM", "InventoryManager/PR.fxml");
        router.loadScene("Item List IM", "InventoryManager/listItemView.fxml");
        
        //Administrator Views
        router.loadScene("Manage Users", "Administrator/UserManagement.fxml");
        router.loadScene("Create PO Admin", "Administrator/CreatePO.fxml");
        router.loadScene("Create PR Admin", "Administrator/CreatePR.fxml");
        router.loadScene("Generate Inventory Report Admin", "Administrator/GenerateInventoryReport.fxml");
        router.loadScene("Item Entry Admin", "Administrator/ItemEntry.fxml");
        router.loadScene("Process Payment Admin", "Administrator/ProcessPayment.fxml");
        router.loadScene("Stock Management Admin", "Administrator/StockManagement.fxml");
        router.loadScene("Supplier Entry Admin", "Administrator/SupplierEntry.fxml");
        router.loadScene("Generate Sales Report", "Administrator/GenerateReport.fxml");
        router.loadScene("Sales Entry Admin", "Administrator/DailySales.fxml");

        
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
            System.out.println(FinanceManagerController.loadPRsFromFileStatic());
            System.out.println(Sales.loadSales());
            
        } catch (IOException e) {
            System.err.print(e);
        }
        launch();
    }

}