package com.talon.testing.controllers;

import com.talon.testing.models.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ListItemController {

    @FXML
    private TableView<Item> itemTableView;

    @FXML
    private TableColumn<Item, String> itemIdColumn;

    @FXML
    private TableColumn<Item, String> itemNameColumn;

    @FXML
    private TableColumn<Item, String> descriptionColumn;

    @FXML
    private TableColumn<Item, String> unitPriceColumn;

    @FXML
    private TableColumn<Item, String> currentStockColumn;

    @FXML
    private TableColumn<Item, String> minimumStockColumn;

    @FXML
    private TableColumn<Item, String> createDateColumn; // [ADDED]

    public void initialize() {
        // Initialize table columns
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        currentStockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        minimumStockColumn.setCellValueFactory(new PropertyValueFactory<>("minimumStock"));
        createDateColumn.setCellValueFactory(new PropertyValueFactory<>("createDate")); // [ADDED]
    }

    public void loadItemData() throws IOException {
        Map<String, Item> items = Item.loadItems();
        ObservableList<Item> allItems = FXCollections.observableArrayList();
        allItems.setAll(items.values());
        itemTableView.setItems(allItems);
    }
}