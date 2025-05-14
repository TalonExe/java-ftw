package com.talon.testing.controllers;

import com.talon.testing.App;
import com.talon.testing.models.Item;
import com.talon.testing.models.Supplier;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class PurchaseManagerController {

    @FXML
    private Button listItemButton;

    @FXML
    private Button listSupplierButton;

    @FXML
    private AnchorPane contentArea;

    @FXML
    public void showItemList() throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("listItemView.fxml"));
        AnchorPane itemView = loader.load();
        ListItemController controller = loader.getController();
        controller.loadItemData();
        contentArea.getChildren().setAll(itemView);
    }

    @FXML
    public void showSupplierList() throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("listSupplierView.fxml"));
        AnchorPane supplierView = loader.load();
        ListSupplierController controller = loader.getController();
        controller.loadSupplierData();
        contentArea.getChildren().setAll(supplierView);
    }

    @FXML
    public void showDisplayRequisition() throws IOException {
        // Load the Display Requisition view
        AnchorPane requisitionView = FXMLLoader.load(App.class.getResource("displayRequisitionView.fxml")); // Create this fxml if you haven't
        contentArea.getChildren().setAll(requisitionView);
    }

    @FXML
    public void showGeneratePo() throws IOException {
        // Load the Generate Purchase Order view
        AnchorPane generatePoView = FXMLLoader.load(App.class.getResource("generatePoView.fxml")); // Create this fxml
        contentArea.getChildren().setAll(generatePoView);
    }

    @FXML
    public void showListPo() throws IOException {
        // Load the List Purchase Orders view
        AnchorPane listPoView = FXMLLoader.load(App.class.getResource("listPoView.fxml")); // Create this fxml
        contentArea.getChildren().setAll(listPoView);
    }
}