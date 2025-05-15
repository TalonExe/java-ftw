package com.talon.testing.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Item {
    private final SimpleStringProperty itemId;
    private final SimpleStringProperty itemName;
    private final SimpleStringProperty description;
    private final SimpleStringProperty unitPrice;
    private final SimpleStringProperty currentStock;
    private final SimpleStringProperty minimumStock;
    private final SimpleStringProperty createDate; // Added property

    // Constructor with createDate (7 parameters)
    public Item(String itemId, String itemName, String description, String unitPrice, String currentStock, String minimumStock, String createDate) {
        this.itemId = new SimpleStringProperty(itemId);
        this.itemName = new SimpleStringProperty(itemName);
        this.description = new SimpleStringProperty(description);
        this.unitPrice = new SimpleStringProperty(unitPrice);
        this.currentStock = new SimpleStringProperty(currentStock);
        this.minimumStock = new SimpleStringProperty(minimumStock);
        this.createDate = new SimpleStringProperty(createDate);
    }

    // Constructor without createDate (6 parameters)
    public Item(String itemId, String itemName, String description, String unitPrice, String currentStock, String minimumStock) {
        this.itemId = new SimpleStringProperty(itemId);
        this.itemName = new SimpleStringProperty(itemName);
        this.description = new SimpleStringProperty(description);
        this.unitPrice = new SimpleStringProperty(unitPrice);
        this.currentStock = new SimpleStringProperty(currentStock);
        this.minimumStock = new SimpleStringProperty(minimumStock);
        this.createDate = new SimpleStringProperty(""); // Default value
    }

    public String getItemId() {
        return itemId.get();
    }

    public SimpleStringProperty itemIdProperty() {
        return itemId;
    }

    public String getItemName() {
        return itemName.get();
    }

    public SimpleStringProperty itemNameProperty() {
        return itemName;
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public String getUnitPrice() {
        return unitPrice.get();
    }

    public SimpleStringProperty unitPriceProperty() {
        return unitPrice;
    }

    public String getCurrentStock() {
        return currentStock.get();
    }

    public SimpleStringProperty currentStockProperty() {
        return currentStock;
    }

    public String getMinimumStock() {
        return minimumStock.get();
    }

    public SimpleStringProperty minimumStockProperty() {
        return minimumStock;
    }

    public String getCreateDate() {
        return createDate.get();
    }

    public SimpleStringProperty createDateProperty() {
        return createDate;
    }

    public static ObservableList<Item> loadItems() throws IOException {
        ObservableList<Item> items = FXCollections.observableArrayList();
        try (BufferedReader br = new BufferedReader(new FileReader("D:\\APU One Drive\\OneDrive - Asia Pacific University\\Documents\\GitHub\\java-ftw\\target\\classes\\data\\items.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 7) {
                    items.add(new Item(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim(), data[4].trim(), data[5].trim(), data[6].trim()));
                } else if (data.length == 6) {
                    items.add(new Item(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim(), data[4].trim(), data[5].trim()));
                } else {
                    System.out.println("Invalid data: " + line);
                }
            }
        }
        return items;
    }

    public static boolean addItem(Item item) throws IOException {
        System.out.println("Adding item functionality needs to be implemented.");
        return false;
    }
}