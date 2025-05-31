package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.utils.FileUtils; // IMPORT FileUtils

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
// Remove unused URL, URISyntaxException

public class StockTransaction {
    private String transactionId;
    private String itemId;
    private String itemNameDisplay;
    private String poId;
    private String transactionType;
    private int quantityChange;
    private int stockLevelAfterTransaction;
    private String transactionTimestamp;
    private String userId;

    private static final String TRANSACTIONS_FILENAME = "stock_transactions.txt"; // Renamed
    private static final Type TRANSACTION_LIST_TYPE = new TypeToken<List<StockTransaction>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ... (Constructors, Getters, Setters, toString remain the same) ...
    public StockTransaction(String itemId, String itemNameDisplay, String poId, String transactionType,
                            int quantityChange, int stockLevelAfterTransaction, String userId) {
        this.transactionId = UUID.randomUUID().toString();
        this.itemId = itemId;
        this.itemNameDisplay = itemNameDisplay;
        this.poId = poId;
        this.transactionType = transactionType;
        this.quantityChange = quantityChange;
        this.stockLevelAfterTransaction = stockLevelAfterTransaction;
        this.transactionTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        this.userId = userId;
    }
    public StockTransaction() {
        this.transactionTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
    public String getTransactionId() { return transactionId; }
    public String getItemId() { return itemId; }
    public String getItemNameDisplay() { return itemNameDisplay; }
    public String getPoId() { return poId; }
    public String getTransactionType() { return transactionType; }
    public int getQuantityChange() { return quantityChange; }
    public int getStockLevelAfterTransaction() { return stockLevelAfterTransaction; }
    public String getTransactionTimestamp() { return transactionTimestamp; }
    public String getUserId() { return userId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemNameDisplay(String itemNameDisplay) { this.itemNameDisplay = itemNameDisplay; }
    public void setPoId(String poId) { this.poId = poId; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }
    public void setStockLevelAfterTransaction(int stockLevelAfterTransaction) { this.stockLevelAfterTransaction = stockLevelAfterTransaction; }
    public void setTransactionTimestamp(String transactionTimestamp) { this.transactionTimestamp = transactionTimestamp; }
    public void setUserId(String userId) { this.userId = userId; }


    public static List<StockTransaction> loadTransactions() throws IOException {
        List<StockTransaction> transactions = new ArrayList<>();
        // Use FileUtils, create if not found with default content "[]" for a list
        File file = FileUtils.getDataFileFromProjectRoot(TRANSACTIONS_FILENAME, true, "[]");

        if (file == null || !file.exists() || !file.canRead()) {
            System.err.println("Cannot read stock transaction data file or file does not exist: " + (file != null ? file.getAbsolutePath() : TRANSACTIONS_FILENAME + " path problem"));
            return transactions;
        }
        // If file.length() is 0 after creation, it should have "[]" due to defaultJsonContent.
        // But if it was an existing empty file not handled by FileUtils init, check here.
        if (file.length() == 0) {
             System.out.println(TRANSACTIONS_FILENAME + " is completely empty. Returning empty list. It should have been initialized with '[]'.");
            return transactions;
        }


        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            transactions = gson.fromJson(reader, TRANSACTION_LIST_TYPE);
            if (transactions == null) { // If JSON content was "null" or just whitespace
                transactions = new ArrayList<>();
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from Stock Transaction data file: " + file.getAbsolutePath() + ". Error: " + e.getMessage());
            throw new IOException("Error parsing Stock Transaction JSON data from " + file.getAbsolutePath(), e);
        }
        return transactions;
    }

    public static void saveTransactions(List<StockTransaction> transactions) throws IOException {
        File file = FileUtils.getDataFileFromProjectRoot(TRANSACTIONS_FILENAME, true, "[]");
        if (file == null) {
            throw new IOException("Could not obtain file path for saving Stock Transactions using filename: " + TRANSACTIONS_FILENAME);
        }
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(transactions, writer);
            System.out.println("Stock Transactions saved to: " + file.getAbsolutePath());
        }
    }
    
    public static void addTransaction(StockTransaction transaction) throws IOException {
        if (transaction == null) return;
        List<StockTransaction> transactions = loadTransactions();
        transactions.add(transaction);
        saveTransactions(transactions);
    }

    // REMOVE the old private static File getFileFromResource method from this class
}