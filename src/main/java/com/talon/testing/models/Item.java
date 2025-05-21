package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

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
        Gson gson = new Gson();
        try (BufferedReader br = new BufferedReader(new FileReader("D:\\APU One Drive\\OneDrive - Asia Pacific University\\Documents\\GitHub\\java-ftw\\target\\classes\\data\\items.txt"))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }

            Type type = new TypeToken<Map<String, ItemData>>(){}.getType();
            Map<String, ItemData> itemMap = gson.fromJson(jsonContent.toString(), type);

            if (itemMap != null) {
                for (ItemData data : itemMap.values()) {
                    if (data.getCreateDate() != null) {
                        items.add(new Item(data.getItemCode(), data.getItemName(), data.getDescription(), data.getUnitPrice(), data.getCurrentStock(), data.getMinimumStock(), data.getCreateDate()));
                    } else {
                        items.add(new Item(data.getItemCode(), data.getItemName(), data.getDescription(), data.getUnitPrice(), data.getCurrentStock(), data.getMinimumStock()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Consider showing an alert to the user about the loading error
        }
        return items;
    }

    // Helper class to match the inner structure of the JSON
    private static class ItemData {
        private String itemCode;
        private String itemName;
        private String description;
        private String unitPrice;
        private String currentStock;
        private String minimumStock;
        private String createDate;

        public String getItemCode() {
            return itemCode;
        }

        public String getItemName() {
            return itemName;
        }

        public String getDescription() {
            return description;
        }

        public String getUnitPrice() {
            return unitPrice;
        }

        public String getCurrentStock() {
            return currentStock;
        }

        public String getMinimumStock() {
            return minimumStock;
        }

        public String getCreateDate() {
            return createDate;
        }
    }

    public static boolean addItem(Item item) throws IOException {
        System.out.println("Adding item functionality needs to be implemented.");
        return false;
    }
}